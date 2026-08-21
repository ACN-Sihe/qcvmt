# Vessel List Page - Technical Specification

## 1. Architecture & Component Tree

This is a legacy JSP-based server-side rendered application using Spring MVC (Spring 3.x) with Hibernate 3 for data persistence. The page follows a traditional request-response cycle without client-side framework.

```mermaid
graph TD
  subgraph sub_page ["Vessel List Page"]
    vesselManageJsp["vesselManage.jsp"]
  end
  subgraph sub_search ["Search Area"]
    searchInput["Search Input"]
    searchBtn["Search Button"]
    importLink["Import Link"]
    addLink["Add Link"]
  end
  subgraph sub_table ["Data Table"]
    dataTable["Vessel Table"]
    pagination["Pagination Control"]
  end
  subgraph sub_actions ["Row Actions"]
    deleteLink["Delete Link"]
    modifyLink["Modify Link"]
  end
  subgraph sub_dialogs ["Related Pages"]
    vesselDetailPage["vesselDetail.jsp Add Form"]
    updateVesselPage["updateVessel.jsp Edit Form"]
    importPageView["importPage.jsp Upload Form"]
    adminPage["admin.jsp Back Target"]
  end
  subgraph sub_backend ["Backend Controllers"]
    cellControl["CellControl.java"]
    userControl["UserControl.java"]
  end
  subgraph sub_dao ["Data Access"]
    vesselDaoImpl["VesselDaoImpl.java"]
    vesselEntity["Vessel.java Entity"]
  end

  vesselManageJsp -->|contains| searchInput
  vesselManageJsp -->|contains| searchBtn
  vesselManageJsp -->|contains| importLink
  vesselManageJsp -->|contains| addLink
  vesselManageJsp -->|contains| dataTable
  vesselManageJsp -->|contains| pagination
  dataTable -->|contains| deleteLink
  dataTable -->|contains| modifyLink

  searchBtn -->|onClick search()| vesselManageJsp
  addLink -->|navigate| vesselDetailPage
  modifyLink -->|navigate| updateVesselPage
  importLink -->|navigate| importPageView
  deleteLink -->|confirm then navigate| cellControl
  vesselDetailPage -->|POST saveVessel| cellControl
  updateVesselPage -->|POST updateVessel| cellControl
  importPageView -->|POST importVessel| userControl

  cellControl -->|calls| vesselDaoImpl
  userControl -->|calls| vesselDaoImpl
  vesselDaoImpl -->|uses| vesselEntity
```

**Component Hierarchy**:
- **vesselManage.jsp**: Main list page (server-side template)
  - Search bar: text input + button (client-side JS navigation)
  - Action links: Import, Add (direct href navigation)
  - Data table: JSTL `<c:forEach>` loop over `${pm.datas}`
  - Pagination: Custom `<pg:pager>` tag library
  - Back button: client-side JS navigation to `all.html`

- **vesselDetail.jsp**: Add vessel form page
  - Form fields: vesselid, deck_hold (select), bay, rowStart, rowEnd, tierStart, tierEnd
  - Client-side validation: `checkValue()` function
  - Submit: POST to `/user/saveVessel.html`

- **updateVessel.jsp**: Edit vessel form page
  - Same fields as vesselDetail.jsp, pre-populated with existing data
  - Hidden field: `id` (primary key)
  - Client-side validation: `checkValue()` function
  - Submit: POST to `/user/updateVessel.html`

- **importPage.jsp**: File upload page
  - File input: `type="file"` for Excel upload
  - Client-side validation: filename non-empty check
  - Submit: POST multipart to `/user/importVessel.html`

> 📎 Source: src/main/webapp/WEB-INF/jsp/vesselManage.jsp; src/main/webapp/WEB-INF/jsp/vesselDetail.jsp; src/main/webapp/WEB-INF/jsp/updateVessel.jsp; src/main/webapp/WEB-INF/jsp/importPage.jsp

## 2. State Management

This is a server-side rendered application with no client-side state management framework. State is managed through:

### Server-Side State
- **Model attributes**: Passed from Controller to JSP via `ModelMap` or `ModelAndView`
  - `pm`: PageManage object containing `datas` (List<Vessel>), `total`, `pagesize`, `offset`
  - `searchKey`: Current search keyword (for preserving in pagination URLs)
  - `result`: Error/success messages from backend operations
  - `vessel`: Vessel entity object (for edit form pre-population)

### Client-Side State
- **DOM state**: Minimal JavaScript variables in inline scripts
  - `searchcontent` input value (read on button click, not stored)
  - Form field values (read during validation, not persisted)
- **No persistent client storage**: No localStorage/sessionStorage usage
- **No reactive state**: No watchers, computed properties, or two-way binding

### Pagination State
- Managed via URL parameter `pager.offset`
- Backend parses offset, calculates page slice (offset to offset+10)
- Current page number highlighted via JSTL conditional: `${currentPageNumber eq pageNumber}`

> 📎 Source: src/main/java/com/springMVC/control/CellControl.java → getVessel() (lines 169-189); src/main/webapp/WEB-INF/jsp/vesselManage.jsp → model attribute access (lines 83-137)

## 3. API Integration

### API Endpoints

| Endpoint | Method | Purpose | Request Params | Response |
|----------|--------|---------|----------------|----------|
| `/user/allVessel.html` | GET | Load all vessels (paginated) | `pager.offset` (int) | ModelAndView("vesselManage") with `pm` model |
| `/user/searchVessel.html` | GET | Search vessels by keyword | `key` (String), `pager.offset` (int) | ModelAndView("vesselManage") with `pm` and `searchKey` |
| `/user/delVessel.html` | GET | Delete vessel by ID | `id` (int, from @ModelAttribute) | Redirect to `/user/allVessel.html` |
| `/user/modifyVessel.html` | GET | Load vessel for editing | `id` (int, from @ModelAttribute) | ModelAndView("updateVessel") with `vessel` model |
| `/user/addVessel.html` | GET | Show add vessel form | None | ModelAndView("vesselDetail") |
| `/user/saveVessel.html` | POST | Create new vessel | Form params: vesselid, deck_hold, bay, rowStart, rowEnd, tierStart, tierEnd | Redirect to `/user/allVessel.html` or return error view |
| `/user/updateVessel.html` | POST | Update existing vessel | Form params: id, vesselid, deck_hold, bay, rowStart, rowEnd, tierStart, tierEnd | Redirect to `/user/allVessel.html` or return error view |
| `/user/importPage.html` | GET | Show import page | None | ModelAndView("importPage") |
| `/user/importVessel.html` | POST | Import vessels from Excel | Multipart file: filename | ModelAndView("importPage") with `result` message |

### Request/Response Schemas

#### GET /user/allVessel.html
**Request**:
```
GET /user/allVessel.html?pager.offset=0
```

**Response Model** (PageManage):
```json
{
  "pm": {
    "total": 150,
    "pagesize": 10,
    "offset": 0,
    "datas": [
      {
        "id": 1,
        "vesselid": "MSC001",
        "deck_hold": "A",
        "bay": "01",
        "rowStart": "01",
        "rowEnd": "10",
        "tierStart": "01",
        "tierEnd": "08"
      }
    ]
  }
}
```

#### GET /user/searchVessel.html
**Request**:
```
GET /user/searchVessel.html?key=MSC&pager.offset=0
```

**Response Model**: Same as allVessel, plus `searchKey` attribute for URL preservation.

#### POST /user/saveVessel.html
**Request** (form-encoded):
```
vesselid=MSC001&deck_hold=A&bay=01&rowStart=01&rowEnd=10&tierStart=01&tierEnd=08
```

**Response**:
- Success: `RedirectView("/user/allVessel.html")`
- Failure (duplicate): `ModelAndView("vesselDetail")` with `result="the vesselid,deck_hold,bay already exists!"`
- Failure (DB error): `ModelAndView("saveVessel")` with `result="The operation failed"`

⚠️ [ERR:no-loading] No loading state indicator during form submission; user may double-click submit button causing duplicate requests
> 📎 Source: src/main/webapp/WEB-INF/jsp/vesselDetail.jsp → form onsubmit (line 190)

#### POST /user/importVessel.html
**Request** (multipart/form-data):
```
Content-Type: multipart/form-data
filename=<Excel file binary>
```

**Response**:
- Success: `ModelAndView("importPage")` (no explicit success message shown to user)
- Failure (N4 vessel not found): `ModelAndView("importPage")` with `result="error_no_vessel_found_in_n4"`
- Failure (empty file): `ModelAndView("importPage")` with `result="import_vessel_file_empty"`

⚠️ [ERR:no-success-feedback] Successful import does not display confirmation message to user; only errors are shown
> 📎 Source: src/main/java/com/springMVC/control/UserControl.java → importVessel() (lines 524-541)

### Error Handling

**Frontend Validation Errors**:
- Displayed in `<tr id="message">` row with red text
- Controlled by `checkValue()` function setting `document.getElementById("show").innerHTML`
- Message row initially hidden (`display:none`), shown on validation failure

**Backend Business Logic Errors**:
- Duplicate key: Returned as `result` model attribute, displayed in `<tr id="ess">` if not empty
- Database errors: Caught in try-catch, generic "The operation failed" message returned

**Unhandled Scenarios**:
- Network errors: No AJAX calls, so no network error handling needed for list/search
- Form submission failures: Page reloads with error message; no retry mechanism

⚠️ [ERR:no-retry] Form submissions have no retry logic; if backend fails, user must manually re-submit after fixing issues
> 📎 Source: src/main/java/com/springMVC/control/CellControl.java → saveVessel() catch block (lines 267-270)

## 4. Data Flow & Transformation

### Data Flow Diagram

```
User Action → Browser HTTP Request → Spring MVC Controller → DAO Layer → Hibernate → Database
                                                                              ↓
Database ← Hibernate ← DAO Layer ← Controller processes result ← ModelAndView ← JSP renders HTML ← Browser displays
```

### Key Data Transformations

#### 1. Search Keyword Encoding
- **Source**: User input in `#searchcontent` text field
- **Transformation**: `encodeURIComponent(key)` in JavaScript
- **Purpose**: Prevent URL injection from special characters
- **Code**: `window.location.href="searchVessel.html?key="+encodeURIComponent(key)`

> 📎 Source: src/main/webapp/WEB-INF/jsp/vesselManage.jsp → search() (line 43)

#### 2. Pagination Offset Parsing
- **Source**: URL parameter `pager.offset`
- **Transformation**: `Integer.parseInt(request.getParameter("pager.offset"))`
- **Fallback**: Default to 0 on NumberFormatException
- **Usage**: Passed to `vesselDao.getAllVessel(offset)` for SQL LIMIT/OFFSET

> 📎 Source: src/main/java/com/springMVC/control/CellControl.java → getVessel() (lines 172-177)

#### 3. Vessel Entity to JSP Display
- **Source**: `List<Vessel>` from DAO
- **Transformation**: JSTL `<c:forEach>` iterates over `${pm.datas}`, accessing properties via getter methods
- **Display mapping**:
  - `${vessel.vesselid}` → Vessel name column
  - `${vessel.deck_hold}` → Deck/Hold column
  - `${vessel.bay}` → BAY column
  - `${vessel.rowStart}` / `${vessel.rowEnd}` → Row range columns
  - `${vessel.tierStart}` / `${vessel.tierEnd}` → Tier range columns

> 📎 Source: src/main/webapp/WEB-INF/jsp/vesselManage.jsp → c:forEach loop (lines 84-98)

#### 4. Form Data to Entity (Save/Update)
- **Source**: HTTP POST form parameters
- **Transformation**: Controller extracts each parameter via `request.getParameter()`, sets on Vessel entity
- **Fields mapped**:
  ```java
  uv.setVesselid(request.getParameter("vesselid"));
  uv.setDeck_hold(request.getParameter("deck_hold"));
  uv.setBay(request.getParameter("bay"));
  uv.setRowStart(request.getParameter("rowStart"));
  uv.setRowEnd(request.getParameter("rowEnd"));
  uv.setTierStart(request.getParameter("tierStart"));
  uv.setTierEnd(request.getParameter("tierEnd"));
  ```

> 📎 Source: src/main/java/com/springMVC/control/CellControl.java → saveVessel() (lines 257-263)

#### 5. Date/Number Formatting
- **No date fields** in Vessel entity
- **Number fields** (bay, rowStart, etc.) stored as String in database (VARCHAR(10))
- **Frontend validation**: `checkNumber()` function in vmt.js validates pure numeric strings
- **No formatting applied**: Values displayed as-is from database

> 📎 Source: src/main/webapp/js/vmt.js → checkNumber() (lines 351-366); src/main/java/com/springMVC/entity/Vessel.java → field definitions (lines 21-40)

#### 6. Enum Mapping (deck_hold)
- **Values**: "A" (Deck), "B" (Hold)
- **Frontend**: `<select>` dropdown with hardcoded options
- **Backend**: Stored as single-character String
- **No enum class**: Magic strings used throughout codebase

> 📎 Source: src/main/webapp/WEB-INF/jsp/vesselDetail.jsp → deck_hold select (lines 199-202)

## 5. Interaction Logic

### Dialog/Navigation Patterns

This application uses **page-level navigation** rather than modal dialogs:

| Action | Pattern | Implementation |
|--------|---------|----------------|
| Add Vessel | Full page navigation | `<a href="addVessel.html">` → vesselDetail.jsp |
| Modify Vessel | Full page navigation | `<a href="modifyVessel.html?id={id}">` → updateVessel.jsp |
| Delete Vessel | Confirm dialog + navigation | `onclick="return confirm('...')"` → delVessel.html |
| Import Vessels | Full page navigation | `<a href="importPage.html">` → importPage.jsp |
| Search | Client-side redirect | `window.location.href` with encoded param |
| Back | Client-side redirect | `window.location.href = "all.html"` |

### Conditional Rendering Rules

#### JSTL Conditionals in vesselManage.jsp
```jsp
<c:choose>
  <c:when test="${!empty pm.datas}">
    <!-- Render table rows -->
  </c:when>
</c:choose>
```
- **Condition**: `pm.datas` is not null and not empty
- **Effect**: Only render table body if there are results
- **Else case**: Empty table (no explicit "no data" message)

```jsp
<c:if test="${! empty searchKey}">
  <c:set var="pageURL" value="searchVessel.html" />
</c:if>
<c:if test="${ empty searchKey}">
  <c:set var="pageURL" value="allVessel.html" />
</c:if>
```
- **Condition**: Presence of `searchKey` model attribute
- **Effect**: Switch pagination base URL between search and list endpoints
- **Purpose**: Preserve search context when paginating

```jsp
<c:choose>
  <c:when test="${currentPageNumber eq pageNumber}">
    <font color="red">${pageNumber}</font>
  </c:when>
  <c:otherwise>
    <a href="${pageUrl}">${pageNumber}</a>
  </c:otherwise>
</c:choose>
```
- **Condition**: Current page number equals rendered page number
- **Effect**: Highlight current page in red, others as clickable links

> 📎 Source: src/main/webapp/WEB-INF/jsp/vesselManage.jsp → conditionals (lines 82-130)

#### JSTL Conditionals in vesselDetail.jsp / updateVessel.jsp
```jsp
<c:if test="${!empty result }">
  <td style="color: red; font-size: 15px;" colspan="2" align="center">${result}</td>
</c:if>
```
- **Condition**: `result` model attribute is not empty
- **Effect**: Display error message in red text below form
- **Trigger**: Backend validation failure (duplicate key) or DB error

> 📎 Source: src/main/webapp/WEB-INF/jsp/vesselDetail.jsp → error display (lines 226-228)

### Form Validation Rules

#### Client-Side Validation (checkValue function)

Executed on form `onsubmit` event; returns `false` to prevent submission if validation fails.

**Validation sequence** (short-circuits on first failure):

1. **vesselid**:
   - Not empty
   - Length ≤ 30

2. **deck_hold**:
   - Not empty
   - Length ≤ 1
   - Value must be "A" or "B"

3. **bay**:
   - Not empty
   - Length ≤ 10
   - Must be numeric (`checkNumber(bay)`)

4. **rowStart**:
   - Not empty
   - Length ≤ 2
   - Must be numeric

5. **rowEnd**:
   - Not empty
   - Length ≤ 3
   - Must be numeric

6. **tierStart**:
   - Not empty
   - Length ≤ 2
   - Must be numeric

7. **tierEnd**:
   - Not empty
   - Length ≤ 3
   - Must be numeric

**Error display mechanism**:
- Set `document.getElementById("show").innerHTML` to error message
- Set `document.getElementById("message").style.display=''` to show error row
- Hide other elements: `document.getElementById("ess").style.display="none"`

> 📎 Source: src/main/webapp/WEB-INF/jsp/vesselDetail.jsp → checkValue() (lines 38-164); src/main/webapp/js/vmt.js → checkNumber() (lines 351-366)

#### Server-Side Validation

**Uniqueness check** (in CellControl.java):
```java
List list = vesselDao.getVesselByCondition(vesselid, deck_hold, bay);
if (list != null && list.size() > 0) {
    model.addAttribute("result", "the vesselid,deck_hold,bay already exists!");
    return new ModelAndView("vesselDetail", model); // or updateVessel
}
```
- **Condition**: Query for existing vessel with same vesselid + deck_hold + bay
- **For update**: Should exclude current record by ID (current implementation does NOT exclude, potential bug)
- **Error message**: Returned via `result` model attribute

⚠️ [ERR:validation-bug] Update uniqueness check does not exclude current record ID, causing false positive when user saves without changes
> 📎 Source: src/main/java/com/springMVC/control/CellControl.java → updateVessel() (lines 218-222)

**Database persistence**:
```java
boolean success = vesselDao.saveOrUpdateVessel(uv);
if (!success) {
    model.addAttribute("result", "The operation failed");
    return new ModelAndView("vesselDetail", model);
}
```
- **Success**: Redirect to list page
- **Failure**: Return to form with generic error message

## 6. Error Handling

### Frontend Error States

| Scenario | Detection | User Feedback | Recovery |
|----------|-----------|---------------|----------|
| Empty required field | `checkValue()` validation | Red text in message row | User corrects input, resubmits |
| Invalid format (non-numeric) | `checkNumber()` validation | Red text: "should be number !" | User corrects input, resubmits |
| Value too long | Length check in `checkValue()` | Red text: "is too long!" | User shortens input, resubmits |
| Invalid deck_hold value | Enum check in `checkValue()` | Red text: "should be 'A' or 'B' !" | User selects valid option, resubmits |

### Backend Error States

| Scenario | Detection | User Feedback | Recovery |
|----------|-----------|---------------|----------|
| Duplicate vessel (unique constraint) | `getVesselByCondition()` returns non-empty list | Red text: "the vesselid,deck_hold,bay already exists!" | User modifies values, resubmits |
| Database save failure | Exception caught in `saveOrUpdateVessel()` | Red text: "The operation failed" | User retries; no specific guidance |
| Import: N4 vessel not found | Exception with message "error_no_vessel_found_in_n4" | Localized error message via `messageUtil.getMessage()` | User corrects Excel file, re-uploads |
| Import: Empty file | Generic exception | Localized error: "import_vessel_file_empty" | User provides valid file, re-uploads |

### Risk Annotations

⚠️ [ERR:no-loading] No loading state during form submission; users may double-click submit button causing duplicate POST requests
> 📎 Source: src/main/webapp/WEB-INF/jsp/vesselDetail.jsp → form onsubmit (line 190)

⚠️ [ERR:no-success-feedback] Successful vessel import does not display confirmation message; user has no feedback that import succeeded
> 📎 Source: src/main/java/com/springMVC/control/UserControl.java → importVessel() (lines 529-530)

⚠️ [ERR:validation-bug] Update uniqueness check queries all vessels including current record, causing false duplicate detection when saving unchanged data
> 📎 Source: src/main/java/com/springMVC/control/CellControl.java → updateVessel() (lines 218-222)

⚠️ [ERR:generic-message] Database save failures return generic "The operation failed" message without root cause details, making debugging difficult for users
> 📎 Source: src/main/java/com/springMVC/control/CellControl.java → saveVessel() catch block (lines 267-270)

⚠️ [ERR:no-empty-state] When vessel list is empty (pm.datas is empty), no "No data" message is displayed; table appears blank without explanation
> 📎 Source: src/main/webapp/WEB-INF/jsp/vesselManage.jsp → c:choose block (lines 82-100)

## 7. Security

### Authentication & Authorization

**Current Implementation**:
- No explicit authentication checks in Controller methods
- All endpoints under `/user/*` path are accessible to any authenticated session
- Logout functionality exists (`logout.html`) but session management is handled by container/framework

**Route Protection**:
- No Spring Security annotations (`@PreAuthorize`, `@Secured`) on Controller methods
- No custom interceptors for permission checking in observed code

⚠️ [OWASP:A01] No route-level authorization checks on vessel CRUD endpoints; any authenticated user can add/modify/delete vessels regardless of role
> 📎 Source: src/main/java/com/springMVC/control/CellControl.java → all @RequestMapping methods (lines 169-271)

### Data Exposure

**Sensitive Data in Logs**:
- `e.printStackTrace()` calls in DAO and Controller layers output stack traces to server logs
- Stack traces may contain sensitive data (user IDs, vessel configurations)

⚠️ [OWASP:A02] Stack traces printed to server logs via e.printStackTrace() may expose sensitive operational data; should use structured logging with log levels
> 📎 Source: src/main/java/com/springMVC/control/CellControl.java → catch blocks (lines 184, 267); src/main/java/com/springMVC/dao/VesselDaoImpl.java → catch blocks (lines 113-115, 140-142)

**Console Logging in JavaScript**:
- `debugMode` flag controls debug output in vmt.js
- When enabled, prints full XHR response text including potentially sensitive data

⚠️ [OWASP:A02] Debug mode in vmt.js prints full XHR responses to DOM; if accidentally enabled in production, exposes API response data to browser inspection
> 📎 Source: src/main/webapp/js/vmt.js → PrintMsg() (lines 184-189), debugMode flag (line 7)

### Input Sanitization

**XSS Prevention**:
- JSP uses `<c:out value="${...}"/>` for displaying user-controlled data (vessel fields)
- `<c:out>` performs HTML entity encoding by default, preventing XSS
- Search keyword passed via URL parameter, encoded with `encodeURIComponent()` on client side

**SQL Injection**:
- Hibernate HQL queries use parameterized queries (`findByNamedParam`, `find` with placeholders)
- No string concatenation in SQL/HQL construction
- Safe from SQL injection

**File Upload Security**:
- Import endpoint accepts multipart file uploads
- No file type validation observed in controller code
- Relies on `importHandler.uploadFile()` for file processing

⚠️ [OWASP:A03] No file type validation on import endpoint; malicious users could upload non-Excel files or files with embedded scripts
> 📎 Source: src/main/java/com/springMVC/control/UserControl.java → importVessel() (lines 524-541)

### CSRF Protection

**Form Submissions**:
- POST forms (saveVessel, updateVessel, importVessel) do not include CSRF tokens
- Spring Security CSRF protection not observed in configuration

⚠️ [OWASP:A01] POST forms lack CSRF tokens; vulnerable to cross-site request forgery attacks where attacker tricks user into submitting malicious vessel data
> 📎 Source: src/main/webapp/WEB-INF/jsp/vesselDetail.jsp → form:form (line 190); src/main/webapp/WEB-INF/jsp/updateVessel.jsp → form:form (line 185)

## 8. Performance

### Rendering Optimization

**Server-Side Rendering**:
- JSP templates rendered on server; no client-side framework overhead
- JSTL `<c:forEach>` iterates over max 10 items per page (fixed pagesize)
- Minimal DOM manipulation; most logic is server-side

**Pagination Strategy**:
- Fixed page size of 10 items
- OFFSET-based pagination: `setFirstResult(offset).setMaxResults(10)`
- ⚠️ **Performance concern**: OFFSET pagination becomes slower with large offsets (database must scan and skip rows)

⚠️ [PERF:large-list] OFFSET-based pagination degrades with large datasets; for 100k+ records, offset=99990 requires scanning 99990 rows before returning 10
> 📎 Source: src/main/java/com/springMVC/dao/VesselDaoImpl.java → getAllVessel() (line 62), searchVessel() (line 159)

### Database Query Optimization

**Query Patterns**:
- `getAllVessel()`: Single HQL query with ORDER BY, LIMIT 10
- `searchVessel()`: HQL with LIKE '%keyword%' on three columns (vesselid, deck_hold, bay)
- **Index recommendation**: Composite index on (vesselid, deck_hold, bay) for uniqueness check; individual indexes on searched columns for LIKE queries

**N+1 Query Problem**:
- Not applicable; single query fetches all vessel data for the page
- No lazy-loaded associations in Vessel entity

### Client-Side Performance

**JavaScript Execution**:
- Inline scripts in JSP pages; no external JS bundling
- `checkValue()` validation runs synchronously on form submit; negligible performance impact
- `checkNumber()` iterates character-by-character; efficient for short strings (max length 10)

**No Lazy Loading**:
- All page resources (CSS, JS, images) loaded upfront
- No image lazy loading implemented

⚠️ [PERF:no-lazy] Logout icon image loaded synchronously on every page; no lazy loading or sprite optimization
> 📎 Source: src/main/webapp/WEB-INF/jsp/vesselManage.jsp → logout img (line 57)

### Bundle Size

**Static Assets**:
- Single JS file: `vmt.js` (~10KB, contains utility functions for time sync, AJAX polling, validation)
- No CSS framework; minimal inline styles in JSP pages
- No third-party libraries except jQuery (referenced in vmt.js but not confirmed loaded on this page)

**JSP Tag Libraries**:
- JSTL core (`http://java.sun.com/jsp/jstl/core`)
- Spring form tags (`http://www.springframework.org/tags/form`)
- Custom pager tag (`http://jsptags.com/tags/navigation/pager`)
- Spring message tags (`http://www.springframework.org/tags`)

### Caching

**No Client-Side Caching**:
- No localStorage/sessionStorage usage
- No service workers or cache headers configured in observed code

**Server-Side Caching**:
- Hibernate first-level cache (Session-scoped) active by default
- No second-level cache configuration observed
- No HTTP response caching headers set

⚠️ [PERF:no-cache] No HTTP caching headers on static assets or API responses; browsers re-fetch resources on every page load
> 📎 Source: src/main/webapp/WEB-INF/jsp/vesselManage.jsp → meta tags (lines 9-10, no cache-control headers)

### Polling & Real-Time Updates

**No Polling**:
- This page uses request-response pattern; no background polling
- vmt.js contains polling logic (`getData()`, `setInterval`) but it's for a different page (QC monitoring), not vessel list

---

## Assumptions & TBDs

1. **Authentication mechanism**: Assumed to be container-managed or Spring Security-based, but specific configuration not examined in this analysis.
2. **Internationalization**: Message keys (e.g., `confirm_delete`, `add`, `delete`) reference resource bundles; actual message texts depend on locale configuration.
3. **Excel import format**: Expected format of imported Excel file not documented; relies on `ImportHandler.importVessel()` implementation details.
4. **N4 system integration**: Error message "error_no_vessel_found_in_n4" suggests integration with external N4 terminal operating system; integration details not examined.
5. **Database schema**: T_Vessel table structure inferred from JPA entity; actual DDL not reviewed.
6. **Session timeout**: No session timeout configuration observed; relies on default servlet container settings.
