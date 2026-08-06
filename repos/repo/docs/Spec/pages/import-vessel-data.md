# Import Vessel Data - Technical Specification

## 1. Architecture & Component Tree

This is a legacy JSP-based page in a Spring MVC application. The architecture follows the traditional Model-View-Controller pattern with server-side rendering.

```mermaid
graph TD
  subgraph sub_page ["Import Vessel Page"]
    importPageJsp["importPage.jsp"]
  end
  subgraph sub_controller ["Controller Layer"]
    userControl["UserControl.java"]
  end
  subgraph sub_service ["Service Layer"]
    importHandler["ImportHandler.java"]
  end
  subgraph sub_dao ["DAO Layer"]
    vesselDao["VesselDao"]
  end
  subgraph sub_entity ["Entity Layer"]
    vesselEntity["Vessel.java"]
  end
  subgraph sub_i18n ["Internationalization"]
    messagesProps["messages_*.properties"]
  end

  importPageJsp -->|POST /user/importVessel.html| userControl
  userControl -->|uploadFile()| importHandler
  userControl -->|importVessel()| importHandler
  importHandler -->|save()| vesselDao
  vesselDao -->|persist| vesselEntity
  userControl -->|messageUtil.getMessage()| messagesProps
  importPageJsp -->|spring:message| messagesProps
```

**Component Hierarchy**:
- **View**: `importPage.jsp` — JSP template with Spring Form tags and JSTL
- **Controller**: `UserControl` — Spring MVC controller handling GET/POST requests
- **Service**: `ImportHandler` — Business logic for file upload and data parsing
- **DAO**: `VesselDao` — Data access layer for T_Vessel table operations
- **Entity**: `Vessel` — JPA entity mapping to T_Vessel table

> 📎 Source: src/main/webapp/WEB-INF/jsp/importPage.jsp; src/main/java/com/springMVC/control/UserControl.java; src/main/java/com/springMVC/util/ImportHandler.java

## 2. State Management

This is a server-side rendered page with no client-side state management framework. State is managed through:

**Server-Side State**:
- `ModelMap model` — Passed from controller to view, contains:
  - `result` — Import result message (error or success), only present after POST submission
  - `user` — Form backing object (Spring Form modelAttribute)

**Client-Side State**:
- No persistent client-side state
- File input value stored in browser's file input element
- No localStorage/sessionStorage usage

**Form State Flow**:
1. GET `/user/importPage.html` → Controller returns empty ModelMap → Page renders without result message
2. POST `/user/importVessel.html` → Controller processes file → Adds `result` to ModelMap on error → Page re-renders with error message
3. Success case → Controller returns ModelAndView without adding result → Page refreshes without message

> 📎 Source: src/main/java/com/springMVC/control/UserControl.java → importVessel(), importPage()

## 3. API Integration

### 3.1 Import Vessel Endpoint

**Endpoint**: `POST /user/importVessel.html`

**Request**:
- Content-Type: `multipart/form-data`
- Form fields:
  - `filename` (file): Uploaded file (.xls, .xlsx, or .txt)

**Response**:
- Returns `ModelAndView("importPage", model)` — Re-renders the same page
- On success: No `result` attribute in model (page refreshes without message)
- On error: `result` attribute contains localized error message

**Error Handling**:
| Error Condition | Exception Message | Displayed Message (i18n key) |
|----------------|-------------------|------------------------------|
| Vessel ID not found in N4 system | `error_no_vessel_found_in_n4` | `error_no_vessel_found_in_n4` |
| File format/content error | Any other exception | `import_vessel_file_empty` |

> 📎 Source: src/main/java/com/springMVC/control/UserControl.java → importVessel()

### 3.2 File Upload Process

**Method**: `ImportHandler.uploadFile(HttpServletRequest, HttpServletResponse)`

**Steps**:
1. Read `uploadFolder` property from `system.properties`
2. Create `DiskFileItemFactory` with 1MB memory threshold
3. Parse multipart request using `ServletFileUpload`
4. Save uploaded file to `{uploadFolder}/{originalFilename}`
5. Return saved `File` object

**⚠️ [OWASP:A01] File upload without extension validation or virus scanning**
> 📎 Source: src/main/java/com/springMVC/util/ImportHandler.java → uploadFile()

The upload method accepts any file type and only validates the extension during parsing. There is no file size limit configuration, no content-type validation, and no malware scanning.

### 3.3 Data Parsing Logic

**Excel Files (.xls/.xlsx)**:
- Uses Apache POI (`HSSFWorkbook`, `HSSFSheet`)
- Skips first row (header)
- Maps columns by index (0-6)
- Handles numeric, string, formula, boolean cell types

**TXT Files**:
- Parses custom format with sections: `*SHIP`, `*STACK`, `*TIER`
- Extracts vessel ID from `*SHIP` section
- Parses tab-delimited data from `*STACK` and optional `*TIER` sections
- Aggregates row/tier ranges by Bay+Level combination

> 📎 Source: src/main/java/com/springMVC/util/ImportHandler.java → getExcelData(), getTxtData()

## 4. Data Flow & Transformation

### 4.1 Excel Data Flow

```
Uploaded .xls file
  ↓
ImportHandler.getExcelData(file, ignoreRows=1)
  ↓
Apache POI reads HSSF workbook
  ↓
For each row (starting from row 1):
  - Column 0 → vessel.setVesselid()
  - Column 1 → vessel.setDeck_hold()
  - Column 2 → vessel.setBay()
  - Column 3 → vessel.setRowStart()
  - Column 4 → vessel.setRowEnd()
  - Column 5 → vessel.setTierStart()
  - Column 6 → vessel.setTierEnd()
  ↓
vesselDao.save(vessel) for each row
  ↓
Persisted to T_Vessel table
```

**Data Type Transformations**:
- Numeric cells: Formatted with `DecimalFormat("0")` to remove decimal places
- Formula cells: Prefer string value, fallback to numeric value
- Boolean cells: Converted to "Y" (true) or "N" (false)
- All values stored as String in database

> 📎 Source: src/main/java/com/springMVC/util/ImportHandler.java → getExcelData(), importVessel()

### 4.2 TXT Data Flow

```
Uploaded .txt file
  ↓
ImportHandler.getTxtData(file)
  ↓
Parse *SHIP section → Extract vesselId
  ↓
Parse *STACK section → Extract bayPlanHeader and bayPlanRows
  ↓
Identify column indices for: STAF BAY, LEVEL, ISO STACK, TOP TIER, BOTTOM TIER
  ↓
For each data row:
  - Group by (STAF BAY + LEVEL)
  - Aggregate: min(ISO STACK) → rowStart, max(ISO STACK) → rowEnd
  - Aggregate: min(BOTTOM TIER) → tierStart, max(TOP TIER) → tierEnd
  ↓
If *TIER section exists:
  - Parse CUSTOM TIER values
  - Override tierStart/tierEnd for matching Bay+Level combinations
  ↓
Validate vesselId exists in N4 via vesselDao.getN4VesselNameById()
  ↓
Convert all numeric values to String
  ↓
Return String[][] array for batch insert
  ↓
vesselDao.save(vessel) for each aggregated record
```

**Aggregation Logic**:
- Multiple rows with same Bay+Level are merged into single Vessel record
- Row range: Takes minimum ISO STACK as start, maximum as end
- Tier range: Takes minimum BOTTOM TIER as start, maximum TOP TIER as end
- Custom Tier section overrides standard Tier values if present

> 📎 Source: src/main/java/com/springMVC/util/ImportHandler.java → getTxtData()

### 4.3 Database Schema

**Table**: `T_Vessel`

| Column | Type | Length | Description |
|--------|------|--------|-------------|
| vmid | Integer (PK, Sequence) | - | Auto-generated primary key (vessel_seq) |
| vesselid | String | 10 | Vessel identifier from N4 system |
| deck_hold | String | 10 | Deck or Hold indicator |
| bay | String | 10 | Bay number |
| rowstart | String | 10 | Row start position |
| rowend | String | 10 | Row end position |
| tierstart | String | 10 | Tier start level |
| tierend | String | 10 | Tier end level |

> 📎 Source: src/main/java/com/springMVC/entity/Vessel.java

## 5. Interaction Logic

### 5.1 Form Validation

**Frontend Validation** (`check()` function):
```javascript
function check(){
    var filename = document.getElementById("filename").value;
    if(filename==null || filename==""){
        alert("filename can't be empty.");
        return false;
    }
    return true;
}
```

- Only checks if file input has a value
- Does not validate file extension or size
- Uses native `alert()` for error display

**Backend Validation**:
- File extension check in `importVessel()`: Only processes .xls, .xlsx, .txt
- TXT format validation: Requires specific section markers and column headers
- N4 vessel existence check: Queries external system before saving

### 5.2 Conditional Rendering

**Result Message Display**:
```jsp
<c:if test="${!empty result }">
    <td style="color:red;font-size:15px;" colspan="2" align="center">${result }</td>
</c:if>
```

- Only renders when `result` model attribute is non-empty
- Styled with inline CSS: red color, 15px font, centered

### 5.3 Navigation

| Action | Target URL | Method |
|--------|-----------|--------|
| Import form submit | `/user/importVessel.html` | POST |
| Back button click | `allVessel.html` | GET (redirect) |
| Logout icon click | `logout.html` | GET (after confirmation) |

> 📎 Source: src/main/webapp/WEB-INF/jsp/importPage.jsp → bback(), sh(), form:form action

## 6. Error Handling

### 6.1 Known Error Scenarios

| Scenario | Detection Point | Error Message Key | User-Facing Message |
|----------|----------------|-------------------|---------------------|
| Empty file selection | Frontend `check()` | N/A (hardcoded alert) | "filename can't be empty." |
| Vessel ID not in N4 | `vesselDao.getN4VesselNameById()` throws exception | `error_no_vessel_found_in_n4` | "N4中没有定义此货船" / "No such vessel found in N4" |
| Invalid TXT format (missing columns) | `getTxtData()` validation | `import_vessel_file_empty` | "请检查文件内容和格式是否正确." / "Please check whether file content is ok or content repetition." |
| General parsing error | Any exception in try-catch block | `import_vessel_file_empty` (fallback) | Same as above |

### 6.2 Risk Annotations

**⚠️ [ERR:no-loading-state] No loading indicator during file upload and processing**
> 📎 Source: src/main/webapp/WEB-INF/jsp/importPage.jsp → form submit

Large file uploads or complex TXT parsing may take significant time, but there is no visual feedback to the user during processing. The browser shows default loading state only.

**⚠️ [ERR:generic-error-handling] Generic catch-all error handler masks specific failures**
> 📎 Source: src/main/java/com/springMVC/control/UserControl.java → importVessel() catch block

All exceptions except `error_no_vessel_found_in_n4` are mapped to the same generic error message `import_vessel_file_empty`. This makes it difficult for users to diagnose issues like database connection failures, file permission errors, or out-of-memory conditions.

**⚠️ [ERR:no-transaction-rollback] Batch insert without transaction management**
> 📎 Source: src/main/java/com/springMVC/util/ImportHandler.java → importVessel() for loop

The method iterates through parsed records and calls `vesselDao.save()` for each. If an error occurs mid-batch, previously saved records are not rolled back, leading to partial data imports.

**⚠️ [ERR:exception-swallowing] File upload exceptions are logged but not propagated**
> 📎 Source: src/main/java/com/springMVC/util/ImportHandler.java → uploadFile() catch block

Exceptions during file upload are caught, logged via `LOG.debug()`, and the method returns `null`. This can cause NullPointerException in the controller when calling `importHandler.importVessel(returnFile)`.

## 7. Security

### 7.1 Authentication & Authorization

- Page access controlled at URL level (`/user/importPage.html`, `/user/importVessel.html`)
- Assumes Spring Security or container-managed security is configured
- No page-level permission checks visible in code

**⚠️ [OWASP:A01] No CSRF protection on file upload form**
> 📎 Source: src/main/webapp/WEB-INF/jsp/importPage.jsp → form:form

The form does not include a CSRF token. While Spring Form tags may add one automatically depending on configuration, this should be verified. File upload endpoints are common CSRF targets.

### 7.2 File Upload Security

**⚠️ [OWASP:A03] Unrestricted file upload allows potential malicious files**
> 📎 Source: src/main/java/com/springMVC/util/ImportHandler.java → uploadFile()

- No file extension whitelist enforcement at upload time
- No content-type validation
- No file size limit (relies on server/container defaults)
- No virus/malware scanning
- File saved with original filename (potential path traversal if filename contains `../`)

**Mitigation in place**:
- File extension checked during parsing (only .xls, .xlsx, .txt processed)
- But attacker could upload .jsp or .exe files that remain on server

### 7.3 Input Sanitization

- File content parsed by Apache POI (Excel) or custom regex/parsing (TXT)
- No SQL injection risk due to JPA/Hibernate usage
- No XSS risk in file content as data is stored in database, not rendered directly

**⚠️ [OWASP:A02] Uploaded filenames stored with original names**
> 📎 Source: src/main/java/com/springMVC/util/ImportHandler.java → uploadFile() line 101-104

Original filenames are preserved when saving to disk. If filenames contain special characters or path traversal sequences, this could lead to file system issues.

## 8. Performance

### 8.1 File Processing Performance

**⚠️ [PERF:large-file] No file size limit may cause OutOfMemoryError**
> 📎 Source: src/main/java/com/springMVC/util/ImportHandler.java → uploadFile(), getExcelData()

- Excel files loaded entirely into memory via Apache POI
- Large Excel files (>10MB) may cause heap space exhaustion
- No streaming or chunked processing implemented

**⚠️ [PERF:n-plus-one] Batch insert uses individual save calls**
> 📎 Source: src/main/java/com/springMVC/util/ImportHandler.java → importVessel() for loop

Each parsed record triggers a separate `vesselDao.save()` call. For files with thousands of rows, this results in N database round-trips instead of batch insert.

### 8.2 Rendering Performance

- Simple JSP page with minimal DOM elements — rendering performance is adequate
- No JavaScript frameworks or heavy client-side processing
- Inline styles used instead of external CSS — minor impact on cacheability

### 8.3 Network Performance

- Single POST request for entire file upload
- No pagination or chunked upload support
- Large files may timeout depending on server configuration

**Recommendation**: Consider implementing file size limits (e.g., 10MB max) and batch database inserts for improved performance.
