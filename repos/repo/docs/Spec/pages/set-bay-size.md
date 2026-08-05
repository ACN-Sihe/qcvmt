# Set Bay Size - Technical Specification

## 1. Architecture & Component Tree

This is a legacy JSP-based page using Spring MVC framework with server-side rendering.

```mermaid
graph TD
  subgraph sub_page ["Set Bay Size Page"]
    setbaysizeJsp["setbaysize.jsp"]
  end
  subgraph sub_controller ["Controller Layer"]
    cellControl["CellControl.java"]
  end
  subgraph sub_dao ["DAO Layer"]
    cellDaoImpl["CellDaoImpl.java"]
  end
  subgraph sub_entity ["Entity Layer"]
    baySize["BaySize.java"]
  end
  subgraph sub_ui ["UI Components"]
    formContainer["Form Container"]
    deckSection["DECK Section"]
    holdSection["HOLD Section"]
    actionButtons["Action Buttons"]
    errorMsg["Error Message Area"]
  end

  setbaysizeJsp -->|GET /user/setbay.html| cellControl
  setbaysizeJsp -->|POST /user/updateBay.html| cellControl
  cellControl -->|getBaySize()| cellDaoImpl
  cellControl -->|updateCellMatrix()| cellDaoImpl
  cellControl -->|model attribute| baySize
  setbaysizeJsp -->|contains| formContainer
  formContainer -->|contains| deckSection
  formContainer -->|contains| holdSection
  formContainer -->|contains| actionButtons
  formContainer -->|conditional| errorMsg
  deckSection -->|inputs| deckRowsInput["deckRows input"]
  deckSection -->|inputs| deckTiersInput["deckTiers input"]
  holdSection -->|inputs| holdRowsInput["holdRows input"]
  holdSection -->|inputs| holdTiersInput["holdTiers input"]
  actionButtons -->|buttons| okBtn["OK submit"]
  actionButtons -->|buttons| cancelBtn["Cancel button"]
```

> 📎 Source: src/main/webapp/WEB-INF/jsp/setbaysize.jsp; src/main/java/com/springMVC/control/CellControl.java; src/main/java/com/springMVC/dao/CellDaoImpl.java; src/main/java/com/springMVC/entity/BaySize.java

## 2. State Management

**Server-Side State**:
- `BaySize` model object holds four string properties: `deckRows`, `deckTiers`, `holdRows`, `holdTiers`
- Model is populated via `cellDao.getBaySize()` on GET request
- Model is bound to form via Spring's `<form:form modelAttribute="baySize">`

**Client-Side State**:
- No JavaScript state management framework used
- Form values stored in HTML input elements
- Error message visibility controlled by JSTL conditional: `<c:if test="${!empty baymsg }">`

**Computed Values**:
- `getRealSize()`: Converts database value to display value by adding 1
- `formatNum()`: Pads single-digit numbers with leading zero (e.g., "5" → "05")

> 📎 Source: src/main/java/com/springMVC/control/CellControl.java → setBaySize(); src/main/java/com/springMVC/dao/CellDaoImpl.java → getRealSize(); src/main/webapp/WEB-INF/jsp/setbaysize.jsp → formatNum()

## 3. API Integration

### GET /user/setbay.html

**Purpose**: Load current bay size configuration

**Request**: No parameters

**Response**: Renders `setbaysize.jsp` with `baySize` model attribute

**Data Flow**:
1. `CellControl.setBaySize()` calls `cellDao.getBaySize()`
2. DAO executes two SQL queries:
   - Query 1: `SELECT MAX(cmrow) AS bayrows, MAX(cmtier) AS baytiers FROM t_cellmatrix WHERE active='1' AND cmtype='A'` (DECK)
   - Query 2: Same query with `cmtype='B'` (HOLD)
3. Results transformed via `getRealSize()` (adds 1 to each value)
4. Values populated into `BaySize` object and passed to view

**Error Handling**:
- Database exceptions caught and logged via `e.printStackTrace()`
- Error message added to model: `baymsg = messageUtil.getMessage("error_query_db_error", request)`

> 📎 Source: src/main/java/com/springMVC/control/CellControl.java → setBaySize(); src/main/java/com/springMVC/dao/CellDaoImpl.java → getBaySize()

⚠️ [ERR:logging] Exception stack trace printed to console without structured logging or user-friendly error handling
> 📎 Source: src/main/java/com/springMVC/control/CellControl.java → setBaySize() line 51

### POST /user/updateBay.html

**Purpose**: Update bay size configuration

**Request Body** (form-encoded):
```
deckRows: string
deckTiers: string
holdRows: string
holdTiers: string
```

**Response**: 
- Success: Redirect to `/user/all.html`
- Failure: Re-render `setbaysize.jsp` with error message

**Data Flow**:
1. `CellControl.updateBay()` receives `@ModelAttribute("baySize") BaySize baySize`
2. **Bug**: `holdTiers` parameter manually extracted from request because `baySize.holdTiers` binding fails (setter name mismatch: `setHoldTier` vs expected `setHoldTiers`)
3. `cellDao.updateCellMatrix(baySize)` executes four UPDATE statements:
   - Update DECK active records: `UPDATE t_cellmatrix SET active='1', cmtier='<deckTiers-1>' WHERE cmrow < <deckRows> AND cmtype='A'`
   - Update HOLD active records: `UPDATE t_cellmatrix SET active='1', cmtier='<holdTiers-1>' WHERE cmrow < <holdRows> AND cmtype='B'`
   - Deactivate excess DECK records: `UPDATE t_cellmatrix SET active='0' WHERE cmrow >= <deckRows> AND cmtype='A'`
   - Deactivate excess HOLD records: `UPDATE t_cellmatrix SET active='0' WHERE cmrow >= <holdRows> AND cmtype='B'`

**Error Handling**:
- `SQLException`: Shows "error_db_not_connected" message
- Other exceptions: Shows "error_can_not_update_bay_size" message

> 📎 Source: src/main/java/com/springMVC/control/CellControl.java → updateBay(); src/main/java/com/springMVC/dao/CellDaoImpl.java → updateCellMatrix()

⚠️ [OWASP:A03] SQL injection vulnerability: Raw string concatenation in SQL queries without parameterized placeholders for tier values
> 📎 Source: src/main/java/com/springMVC/dao/CellDaoImpl.java → updateCellMatrix() lines 1580, 1587

## 4. Data Flow & Transformation

### Read Path (GET)

```
Database (t_cellmatrix)
  ↓ SELECT MAX(cmrow), MAX(cmtier) WHERE active='1' AND cmtype='A'/'B'
CellDaoImpl.getBaySize()
  ↓ getRealSize(value) → Integer.parseInt(value) + 1
BaySize entity (deckRows, deckTiers, holdRows, holdTiers)
  ↓ Spring ModelMap
CellControl.setBaySize()
  ↓ ModelAndView("setbaysize", model)
JSP View (setbaysize.jsp)
  ↓ <c:out value="${baySize.deckRows}"/>
HTML Input Fields
```

### Write Path (POST)

```
HTML Input Fields
  ↓ Form submission with client-side validation (checkv())
  ↓ formatNum(value) → pad with leading zero if < 10
CellControl.updateBay()
  ↓ @ModelAttribute binding (with manual holdTiers fix)
BaySize entity
  ↓ updateCellMatrix(baySize)
CellDaoImpl.updateCellMatrix()
  ↓ Integer.valueOf(tiers) - 1 (reverse transformation)
  ↓ Four UPDATE statements on t_cellmatrix
Database (t_cellmatrix)
```

### Key Transformations

| Operation | Direction | Formula | Location |
|-----------|-----------|---------|----------|
| Display conversion | DB → UI | `displayValue = dbValue + 1` | `CellDaoImpl.getRealSize()` |
| Storage conversion | UI → DB | `dbValue = inputValue - 1` (for tiers only) | `CellDaoImpl.updateCellMatrix()` |
| Number formatting | UI → UI | `value < 10 ? "0" + value : value` | `setbaysize.jsp.formatNum()` |

> 📎 Source: src/main/java/com/springMVC/dao/CellDaoImpl.java → getRealSize(), updateCellMatrix(); src/main/webapp/WEB-INF/jsp/setbaysize.jsp → formatNum()

## 5. Interaction Logic

### Form Validation (Client-Side)

**Function**: `checkv()` - executed on form submit via `onsubmit="return checkv();"`

**Validation Sequence** (applied to each field in order: deckRows → deckTiers → holdRows → holdTiers):

1. **Empty check**: `if (obj.value == "")` → alert and return false
2. **Numeric check**: `reg.test(obj.value)` where `reg = /^[0-9]*$/` → alert and return false
3. **Max value check**: `if (obj.value > 100)` → alert and return false
4. **Min value check**: `if (obj.value < 5)` → alert and return false
5. **Format**: Apply `formatNum()` to pad single digits

**Note**: Validation uses loose comparison (`>` and `<`) which may cause type coercion issues with string values.

> 📎 Source: src/main/webapp/WEB-INF/jsp/setbaysize.jsp → checkv()

### Conditional Rendering

**Error Message Display**:
```jsp
<c:if test="${!empty baymsg }">
    <td style="color:red;font-size:15px;" colspan="2" align="center">${result }</td>
</c:if>
```

- Condition: `baymsg` model attribute is not empty
- Content: Displays `${result}` (note: potential bug - should likely be `${baymsg}`)
- Styling: Red text, 15px font, centered

> 📎 Source: src/main/webapp/WEB-INF/jsp/setbaysize.jsp → c:if block

⚠️ [ERR:logic] Error message displays `${result}` instead of `${baymsg}`, which may show undefined content
> 📎 Source: src/main/webapp/WEB-INF/jsp/setbaysize.jsp → line 160

### Navigation

**Cancel Button**: Direct navigation via `window.location.href="all.html"`

**Success Redirect**: Server-side redirect via `new ModelAndView("redirect:/user/all.html")`

> 📎 Source: src/main/webapp/WEB-INF/jsp/setbaysize.jsp → back(); src/main/java/com/springMVC/control/CellControl.java → updateBay()

## 6. Error Handling

### Client-Side Errors

| Error Type | Trigger | User Feedback |
|------------|---------|---------------|
| Empty field | Field value is empty string | Alert dialog with field-specific message |
| Non-numeric input | Value doesn't match `/^[0-9]*$/` | Alert dialog: "must be a nubmer!" |
| Value too large | Value > 100 | Alert dialog: "must not more than 100!" |
| Value too small | Value < 5 | Alert dialog: "must not less than 5!" |

### Server-Side Errors

| Error Type | HTTP Status | User Feedback |
|------------|-------------|---------------|
| Database query failure (GET) | 200 (re-render) | Red error message: "error_query_db_error" |
| SQLException (POST) | 200 (re-render) | Red error message: "error_db_not_connected" |
| General exception (POST) | 200 (re-render) | Red error message: "error_can_not_update_bay_size" |

⚠️ [ERR:no-loading] No loading indicator during form submission; user may click submit multiple times
> 📎 Source: src/main/webapp/WEB-INF/jsp/setbaysize.jsp → form submit

⚠️ [ERR:no-debounce] No debounce mechanism on submit button; concurrent submissions possible
> 📎 Source: src/main/webapp/WEB-INF/jsp/setbaysize.jsp → form submit

## 7. Security

### Authentication

- Page access requires authenticated session (managed by Spring Security or custom filter)
- Logout functionality available via top-right icon

### Authorization

- No role-based access control visible at page level
- All authenticated users can modify bay size configuration

### Input Sanitization

⚠️ [OWASP:A03] SQL injection risk: String concatenation used in UPDATE queries for tier values without prepared statement parameters
> 📎 Source: src/main/java/com/springMVC/dao/CellDaoImpl.java → updateCellMatrix() lines 1580, 1587

```java
// Vulnerable code pattern:
String sql = "update t_cellmatrix set active='1' ,cmtier='" + deckTier + "' where  cmrow<?  and cmtype=?";
```

**Recommendation**: Use parameterized queries for all dynamic values:
```java
query.setString(2, deckTier); // Instead of string concatenation
```

⚠️ [OWASP:A02] No CSRF token protection visible in form submission
> 📎 Source: src/main/webapp/WEB-INF/jsp/setbaysize.jsp → form:form

**Note**: Spring's `<form:form>` tag typically includes CSRF token automatically if Spring Security is configured, but this should be verified.

### Data Exposure

- No sensitive data (passwords, tokens) present in this page
- Bay size configuration is operational data, not personally identifiable information

## 8. Performance

### Rendering

- Simple JSP template with minimal DOM elements (~20 elements)
- No client-side framework overhead
- CSS uses legacy IE compatibility expressions (`height: expression(...)`)

⚠️ [PERF:legacy-css] IE-specific CSS expressions (`expression()`) are deprecated and may cause performance issues in modern browsers
> 📎 Source: src/main/webapp/WEB-INF/jsp/setbaysize.jsp → line 15

### Database Operations

**GET Request**: Two separate SQL queries with `MAX()` aggregation
- Query complexity: O(n) scan on t_cellmatrix table
- No visible indexing strategy documented

**POST Request**: Four sequential UPDATE statements
- Each UPDATE scans t_cellmatrix table based on cmrow and cmtype conditions
- No transaction boundary explicitly defined (relies on Hibernate session management)

⚠️ [PERF:no-transaction] Multiple UPDATE operations not wrapped in explicit transaction; partial failure may leave data in inconsistent state
> 📎 Source: src/main/java/com/springMVC/dao/CellDaoImpl.java → updateCellMatrix()

### Optimization Opportunities

1. Combine two GET queries into single query with GROUP BY cmtype
2. Add database indexes on `(cmtype, active, cmrow)` and `(cmtype, active, cmtier)` columns
3. Wrap POST updates in explicit transaction with rollback on failure
4. Replace string concatenation in SQL with parameterized queries (also security improvement)
