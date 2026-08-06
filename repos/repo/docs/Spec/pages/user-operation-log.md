# User Operation Log Page - Technical Specification

## 1. Architecture & Component Tree

This is a legacy JSP-based server-side rendered page using Spring MVC framework with JSTL tag libraries.

```mermaid
graph TD
  subgraph sub_page ["User Operation Log Page"]
    logJsp["log.jsp"]
  end
  subgraph sub_controller ["Controller Layer"]
    userControl["UserControl"]
  end
  subgraph sub_dao ["DAO Layer"]
    userDao["UserDaoImpl"]
  end
  subgraph sub_entity ["Entity Layer"]
    showLog["ShowLog Entity"]
    pageManage["PageManage"]
  end
  subgraph sub_security ["Security Layer"]
    securityInterceptor["SecurityInterceptor"]
  end

  logJsp -->|GET /user/log.html| userControl
  userControl -->|getUserLog()| userDao
  userDao -->|Query T_SHOWLOG| showLog
  userDao -->|Return paginated data| pageManage
  securityInterceptor -->|Intercepts all requests| userControl
```

**Component Hierarchy:**
- **View**: `src/main/webapp/WEB-INF/jsp/log.jsp` - JSP template with JSTL tags
- **Controller**: `src/main/java/com/springMVC/control/UserControl.java` - Spring MVC controller handling `/user/log` endpoint
- **DAO**: `src/main/java/com/springMVC/dao/UserDaoImpl.java` - Hibernate-based data access layer
- **Entity**: `src/main/java/com/springMVC/entity/ShowLog.java` - JPA entity mapped to T_SHOWLOG table
- **Pagination**: `src/main/java/com/springMVC/entity/PageManage.java` - Pagination wrapper object
- **Security**: `src/main/java/com/springMVC/filter/SecurityInterceptor.java` - Session-based authentication interceptor

> 📎 Source: src/main/webapp/WEB-INF/jsp/log.jsp; src/main/java/com/springMVC/control/UserControl.java → showLog()

## 2. State Management

This is a server-side rendered page with no client-side state management. All state is managed on the server:

**Server-Side State:**
- **Session Attributes**: 
  - `Constants.USER_LOGIN` - Current logged-in user object (used for security check)
  - `Constants.QC_ID` - Current QC identifier
- **Request Parameters**:
  - `userid` - Target user ID for log query (from URL parameter or hidden form field)
  - `pager.offset` - Pagination offset (integer, defaults to 0)
- **Model Attributes**:
  - `pm` - PageManage object containing paginated log data
  - `pm.datas` - List of ShowLog entities
  - `pm.total` - Total record count
  - `pm.pagesize` - Page size (fixed at 10)
  - `pm.userid` - Current user ID for pagination links

**Hidden Form Field:**
- `<input type="hidden" value="${pm.userid}" id="idd"/>` - Preserves userid across pagination requests

> 📎 Source: src/main/java/com/springMVC/control/UserControl.java → showLog(); src/main/webapp/WEB-INF/jsp/log.jsp → hidden input

## 3. API Integration

### Endpoint: GET /user/log.html

**Request Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| id | int | Optional | User ID from ModelAttribute binding |
| userid | int | Optional | User ID from request parameter (fallback) |
| pager.offset | int | Optional | Pagination offset (defaults to 0) |

**Response:**
- Renders `log.jsp` view with model attribute `pm` (PageManage object)
- PageManage structure:
  ```json
  {
    "datas": [
      {
        "id": 1,
        "userid": 100,
        "username": "admin",
        "qcid": "QC001",
        "loginTime": "20240101120000",
        "operation": "LOGIN"
      }
    ],
    "total": 50,
    "pagesize": 10,
    "offset": 0,
    "userid": 100
  }
  ```

**Backend Query Logic:**
- Time range: Last month (calculated by `WebUtil.getPreMonthTime()` to `WebUtil.getTime()`)
- Filter: `userid = ? AND loginTime BETWEEN ? AND ?`
- Order: `loginTime DESC`
- Pagination: `setFirstResult(offset).setMaxResults(10)`

> 📎 Source: src/main/java/com/springMVC/control/UserControl.java → showLog(); src/main/java/com/springMVC/dao/UserDaoImpl.java → getUserLog()

### Related Endpoints

**GET /user/all.html** - Admin user list page (navigation target via BACK button)
**GET /user/logout.html** - Logout endpoint (triggered by logout icon)

## 4. Data Flow & Transformation

### Data Flow Diagram

```
Browser Request (GET /user/log.html?userid=100&pager.offset=0)
    ↓
SecurityInterceptor.preHandle() - Check session for USER_LOGIN
    ↓
UserControl.showLog() - Extract userid and offset parameters
    ↓
UserDaoImpl.getUserLog(userid, offset)
    ↓
Hibernate Query: SELECT FROM ShowLog WHERE userid=? AND loginTime BETWEEN ? AND ? ORDER BY loginTime DESC
    ↓
Database (T_SHOWLOG table)
    ↓
List<ShowLog> entities returned
    ↓
PageManage object populated (datas, total=COUNT, pagesize=10, offset, userid)
    ↓
ModelAndView("log", model) - Render log.jsp
    ↓
JSP Template Processing:
  - Iterate pm.datas with c:forEach
  - Parse loginTime: yyyyMMddHHmmss → Date object
  - Format Date: yyyy-MM-dd HH:mm:ss
  - Render pagination links with pg:pager taglib
    ↓
HTML Response to Browser
```

### Data Transformation Rules

**Time Format Conversion:**
```jsp
<fmt:parseDate value="${log.loginTime}" pattern="yyyyMMddHHmmss" var="test"/>
<fmt:formatDate value="${test}" pattern="yyyy-MM-dd HH:mm:ss"/>
```
- Input: String in format `yyyyMMddHHmmss` (e.g., "20240101120000")
- Output: String in format `yyyy-MM-dd HH:mm:ss` (e.g., "2024-01-01 12:00:00")

**Internationalization:**
All UI text uses `<spring:message>` tags referencing resource bundles:
- `messages_en.properties` - English
- `messages_zh_CN.properties` - Simplified Chinese
- `messages_zh_TW.properties` - Traditional Chinese

> 📎 Source: src/main/webapp/WEB-INF/jsp/log.jsp → fmt:parseDate/fmt:formatDate; src/main/resources/messages_en.properties

## 5. Interaction Logic

### Dialog/Modal Patterns

**Logout Confirmation:**
- Uses browser native `window.confirm()` dialog
- Message key: `confirm_logout` ("Are you sure to logout?")
- On confirm: Redirect to `logout.html`
- On cancel: No action

```javascript
function sh(){
    if(window.confirm("<spring:message code="confirm_logout" />")){
        window.location.href="logout.html";
    }
}
```

> 📎 Source: src/main/webapp/WEB-INF/jsp/log.jsp → sh() function

### Conditional Rendering

**Empty Data Handling:**
```jsp
<c:if test="${! empty pm.datas}">
    <c:forEach var="log" items="${pm.datas}">
        <!-- Render row -->
    </c:forEach>
</c:if>
```
- When `pm.datas` is null or empty, no data rows are rendered
- Table headers and pagination controls remain visible

**Current Page Highlight:**
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

> 📎 Source: src/main/webapp/WEB-INF/jsp/log.jsp → c:if and c:choose blocks

### Form Validation

**No client-side validation** - This is a read-only display page with no form inputs except the hidden userid field and submit button for navigation.

**Server-side parameter parsing:**
```java
try {
    offset = Integer.parseInt(request.getParameter("pager.offset"));
} catch (NumberFormatException ex) {
    // Defaults to 0
}
```
- Invalid integer parameters default to 0 without error messages

> 📎 Source: src/main/java/com/springMVC/control/UserControl.java → showLog() lines 460-465

## 6. Error Handling

### Current Error Handling

**Parameter Parsing Errors:**
- `NumberFormatException` when parsing `pager.offset` or `userid` is caught silently
- Default values (0) are used without user notification

⚠️ [ERR:silent-failure] NumberFormatException during parameter parsing is caught but not logged or communicated to user
> 📎 Source: src/main/java/com/springMVC/control/UserControl.java → showLog() lines 446-458

**Database Query Errors:**
- Exceptions during `userDao.getUserLog()` are caught and printed to stack trace
- `PageManage log` remains null if exception occurs
- No error message displayed to user; page renders with null `pm` attribute

⚠️ [ERR:null-model] Database query failure results in null pm attribute without error feedback to user
> 📎 Source: src/main/java/com/springMVC/control/UserControl.java → showLog() lines 467-472

**Missing Security Context:**
- If session expires, SecurityInterceptor redirects to login page
- Error message "Please login first!" is set in session but may not be displayed on login page

### Risk Annotations

⚠️ [ERR:silent-failure] Silent exception handling in parameter parsing - invalid userid or offset values fail without user notification
> 📎 Source: src/main/java/com/springMVC/control/UserControl.java → showLog() lines 446-458

⚠️ [ERR:null-model] Database query exceptions result in null PageManage object, causing potential NullPointerException during JSP rendering
> 📎 Source: src/main/java/com/springMVC/control/UserControl.java → showLog() lines 467-472

## 7. Security

### Authentication & Authorization

**Session-Based Authentication:**
- `SecurityInterceptor` checks for `Constants.USER_LOGIN` attribute in session
- Unauthenticated requests are redirected to `/index.jsp`
- Excluded URLs can be configured via `excludedUrls` list

> 📎 Source: src/main/java/com/springMVC/filter/SecurityInterceptor.java → preHandle()

**Access Control:**
- Only authenticated users can access the log page
- Logs are filtered by `userid` parameter, preventing cross-user data access
- The `userid` is passed via URL parameter and hidden form field

⚠️ [OWASP:A01] userid parameter is passed via URL and can be manipulated to view other users' logs if attacker knows valid user IDs. No additional authorization check verifies that the requesting admin has permission to view the target user's logs.
> 📎 Source: src/main/java/com/springMVC/control/UserControl.java → showLog() line 454; src/main/webapp/WEB-INF/jsp/log.jsp → hidden input id="idd"

**Data Exposure:**
- User IDs are exposed in URL parameters (`log.html?id=${user.id}`)
- Hidden form fields contain user IDs (`<input type="hidden" value="${pm.userid}">`)

⚠️ [OWASP:A02] User IDs exposed in URL parameters and hidden form fields could facilitate enumeration attacks
> 📎 Source: src/main/webapp/WEB-INF/jsp/admin.jsp → log.html?id=${user.id}; src/main/webapp/WEB-INF/jsp/log.jsp → hidden input

**CSRF Protection:**
- No CSRF token implementation found in the application
- GET requests perform state-changing operations (logout via GET)

⚠️ [OWASP:A01] No CSRF protection implemented; logout operation uses GET method which violates HTTP semantics and is vulnerable to CSRF attacks
> 📎 Source: src/main/webapp/WEB-INF/jsp/log.jsp → onclick="sh()" redirects to logout.html via GET

**XSS Prevention:**
- User data is rendered using `<c:out>` which provides automatic HTML escaping
- No direct use of `${variable}` without escaping in data display areas

✅ XSS mitigation present via JSTL c:out tags
> 📎 Source: src/main/webapp/WEB-INF/jsp/log.jsp → c:out value="${log.username}"

## 8. Performance

### Rendering Optimization

**Pagination:**
- Fixed page size of 10 records per page
- Database-level pagination using `setFirstResult()` and `setMaxResults()`
- Prevents loading entire dataset into memory

> 📎 Source: src/main/java/com/springMVC/dao/UserDaoImpl.java → getUserLog() line 150

**Time Range Filtering:**
- Backend automatically limits query to last month's data
- Reduces dataset size and improves query performance

> 📎 Source: src/main/java/com/springMVC/dao/UserDaoImpl.java → getUserLog() lines 138-139

### Risk Annotations

⚠️ [PERF:no-index] No evidence of database index on T_SHOWLOG.USERID or T_SHOWLOG.LOGINTIME columns, which could cause slow queries for users with many log entries
> 📎 Source: src/main/java/com/springMVC/entity/ShowLog.java → @Table(name = "T_SHOWLOG"); @Column annotations lack index specifications

⚠️ [PERF:n-plus-1] If ShowLog entity has lazy-loaded associations, iterating over pm.datas could trigger N+1 queries (though current entity appears to have only basic columns)
> 📎 Source: src/main/java/com/springMVC/entity/ShowLog.java → Entity definition with basic columns only

⚠️ [PERF:legacy-css] CSS uses deprecated `expression()` function for IE compatibility (`height: expression((documentElement.clientHeight > 320) ? "320px" : "100%")!important`), which is not supported in modern browsers and adds processing overhead
> 📎 Source: src/main/webapp/WEB-INF/jsp/log.jsp → style block line 18
