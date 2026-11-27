package com.campus.placement.rest;

import com.campus.placement.db.DatabaseInitializer;
import com.campus.placement.model.*;
import com.campus.placement.service.*;
import com.campus.placement.util.LoggerUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Optional, isolated interactive REST server built with the JDK's built-in
 * {@link HttpServer} ({@code com.sun.net.httpserver}).
 *
 * <p>Powers the interactive web UI at {@code GET /} without modifying the core
 * JavaFX desktop application.</p>
 */
public final class PlacementRestServer {

    private static final Logger LOGGER = LoggerUtil.getLogger(PlacementRestServer.class);
    private static final int DEFAULT_PORT = 8080;

    private final HttpServer server;
    private final AuthService authService = new AuthService();
    private final CompanyService companyService = new CompanyService();
    private final DriveService driveService = new DriveService();
    private final StudentService studentService = new StudentService();
    private final EligibilityService eligibilityService = new EligibilityService();
    private final ApplicationService applicationService = new ApplicationService();
    private final OfferService offerService = new OfferService();
    private final NotificationService notificationService = new NotificationService();
    private final ReportService reportService = new ReportService();

    public PlacementRestServer(int port) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        this.server.createContext("/", this::handleStaticIndex);
        this.server.createContext("/api/stats", this::handleStats);
        this.server.createContext("/api/login", this::handleLogin);
        this.server.createContext("/api/register", this::handleRegister);
        this.server.createContext("/api/companies", this::handleCompanies);
        this.server.createContext("/api/drives", this::handleDrives);
        this.server.createContext("/api/students", this::handleStudents);
        this.server.createContext("/api/students/block", this::handleStudentBlock);
        this.server.createContext("/api/applications", this::handleApplications);
        this.server.createContext("/api/applications/stage", this::handleApplicationStage);
        this.server.createContext("/api/applications/history", this::handleApplicationHistory);
        this.server.createContext("/api/offers", this::handleOffers);
        this.server.createContext("/api/offers/respond", this::handleOfferRespond);
        this.server.createContext("/api/notifications", this::handleNotifications);
        this.server.createContext("/api/notifications/read", this::handleNotificationRead);
    }

    public void start() {
        server.start();
        LOGGER.info("PlacementRestServer listening on http://localhost:" + server.getAddress().getPort());
    }

    public void stop() {
        server.stop(0);
    }

    private void handleStaticIndex(HttpExchange ex) throws IOException {
        if (!"GET".equalsIgnoreCase(ex.getRequestMethod())) {
            sendJson(ex, 405, "{\"error\":\"Method Not Allowed\"}");
            return;
        }
        try (InputStream in = PlacementRestServer.class.getResourceAsStream("/web/index.html")) {
            if (in == null) {
                sendJson(ex, 404, "{\"error\":\"index.html not found\"}");
                return;
            }
            byte[] bytes = in.readAllBytes();
            ex.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
            ex.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = ex.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    private void handleStats(HttpExchange ex) throws IOException {
        try {
            DashboardStats s = reportService.getDashboardStats();
            String json = "{"
                    + "\"totalStudents\":" + s.getTotalStudents() + ","
                    + "\"placedStudents\":" + s.getPlacedStudents() + ","
                    + "\"placedPercentage\":" + String.format(Locale.US, "%.2f", s.getPlacedPercentage()) + ","
                    + "\"activeDrives\":" + s.getActiveDrives() + ","
                    + "\"totalOffers\":" + s.getTotalOffers() + ","
                    + "\"averagePackageLpa\":" + String.format(Locale.US, "%.2f", s.getAveragePackageLpa()) + ","
                    + "\"highestPackageLpa\":" + String.format(Locale.US, "%.2f", s.getHighestPackageLpa()) + ","
                    + "\"placedByBranch\":" + mapToJson(s.getPlacedByBranch()) + ","
                    + "\"packageDistribution\":" + mapToJson(s.getPackageDistribution())
                    + "}";
            sendJson(ex, 200, json);
        } catch (Exception e) {
            sendError(ex, 500, e);
        }
    }

    private void handleLogin(HttpExchange ex) throws IOException {
        if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
            sendJson(ex, 405, "{\"error\":\"POST required\"}");
            return;
        }
        try {
            Map<String, String> body = parseJson(readBody(ex));
            User u = authService.login(body.get("email"), body.get("password"));
            Student s = u.getRole() == Role.STUDENT ? studentService.getByUserId(u.getId()).orElse(null) : null;
            StringBuilder sb = new StringBuilder("{");
            sb.append("\"id\":").append(u.getId()).append(',')
              .append("\"email\":").append(quote(u.getEmail())).append(',')
              .append("\"role\":").append(quote(u.getRole().name())).append(',')
              .append("\"companyId\":").append(u.getCompanyId() != null ? u.getCompanyId() : "null");
            if (s != null) {
                sb.append(",\"student\":").append(studentToJson(s));
            }
            sb.append('}');
            sendJson(ex, 200, sb.toString());
        } catch (Exception e) {
            sendError(ex, 400, e);
        }
    }

    private void handleRegister(HttpExchange ex) throws IOException {
        if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
            sendJson(ex, 405, "{\"error\":\"POST required\"}");
            return;
        }
        try {
            Map<String, String> body = parseJson(readBody(ex));
            Student s = new Student();
            s.setRollNo(body.get("rollNo"));
            s.setFullName(body.get("fullName"));
            s.setEmail(body.get("email"));
            s.setPhone(body.get("phone"));
            s.setBranch(body.get("branch"));
            s.setBatchYear(Integer.parseInt(body.getOrDefault("batchYear", "2026")));
            s.setCgpa(Double.parseDouble(body.getOrDefault("cgpa", "0")));
            s.setActiveBacklogs(Integer.parseInt(body.getOrDefault("activeBacklogs", "0")));
            s.setSkills(body.getOrDefault("skills", ""));
            Student saved = authService.registerStudent(s, body.get("password"));
            sendJson(ex, 200, studentToJson(saved));
        } catch (Exception e) {
            sendError(ex, 400, e);
        }
    }

    private void handleCompanies(HttpExchange ex) throws IOException {
        try {
            String method = ex.getRequestMethod();
            if ("GET".equalsIgnoreCase(method)) {
                List<Company> list = companyService.getAllCompanies();
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < list.size(); i++) {
                    if (i > 0) sb.append(',');
                    Company c = list.get(i);
                    sb.append('{')
                      .append("\"id\":").append(c.getId()).append(',')
                      .append("\"name\":").append(quote(c.getName())).append(',')
                      .append("\"industry\":").append(quote(c.getIndustry())).append(',')
                      .append("\"contactPerson\":").append(quote(c.getContactPerson())).append(',')
                      .append("\"contactEmail\":").append(quote(c.getContactEmail())).append(',')
                      .append("\"hrPhone\":").append(quote(c.getHrPhone()))
                      .append('}');
                }
                sendJson(ex, 200, sb.append(']').toString());
            } else if ("POST".equalsIgnoreCase(method)) {
                Map<String, String> b = parseJson(readBody(ex));
                Company c = new Company();
                c.setName(b.get("name"));
                c.setIndustry(b.get("industry"));
                c.setWebsite(b.getOrDefault("website", ""));
                c.setContactPerson(b.get("contactPerson"));
                c.setContactEmail(b.get("contactEmail"));
                c.setHrPhone(b.get("hrPhone"));
                int actorId = Integer.parseInt(b.getOrDefault("actorUserId", "1"));
                Company saved = companyService.createCompany(c, actorId);
                sendJson(ex, 200, "{\"id\":" + saved.getId() + ",\"name\":" + quote(saved.getName()) + "}");
            } else if ("DELETE".equalsIgnoreCase(method)) {
                Map<String, String> q = parseQuery(ex.getRequestURI().getRawQuery());
                int id = Integer.parseInt(q.get("id"));
                int actorId = Integer.parseInt(q.getOrDefault("actorUserId", "1"));
                companyService.deleteCompany(id, actorId);
                sendJson(ex, 200, "{\"deleted\":true}");
            } else {
                sendJson(ex, 405, "{\"error\":\"Method Not Allowed\"}");
            }
        } catch (Exception e) {
            sendError(ex, 400, e);
        }
    }

    private void handleDrives(HttpExchange ex) throws IOException {
        try {
            String method = ex.getRequestMethod();
            if ("GET".equalsIgnoreCase(method)) {
                Map<String, String> q = parseQuery(ex.getRequestURI().getRawQuery());
                String keyword = q.get("keyword");
                String statusStr = q.get("status");
                DriveStatus status = (statusStr != null && !statusStr.isBlank() && !"ALL".equals(statusStr))
                        ? DriveStatus.valueOf(statusStr) : null;
                String sortBy = q.get("sort");
                List<Drive> drives = driveService.searchDrives(keyword, status, sortBy);

                Student student = null;
                if (q.containsKey("studentId") && !q.get("studentId").isBlank()) {
                    student = studentService.getById(Integer.parseInt(q.get("studentId"))).orElse(null);
                }
                if (q.containsKey("companyId") && !q.get("companyId").isBlank()) {
                    int compId = Integer.parseInt(q.get("companyId"));
                    drives = drives.stream().filter(d -> d.getCompanyId() == compId).toList();
                }

                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < drives.size(); i++) {
                    if (i > 0) sb.append(',');
                    Drive d = drives.get(i);
                    EligibilityResult elig = student != null ? eligibilityService.checkEligibility(student, d) : null;
                    sb.append('{')
                      .append("\"id\":").append(d.getId()).append(',')
                      .append("\"companyId\":").append(d.getCompanyId()).append(',')
                      .append("\"company\":").append(quote(d.getCompanyName())).append(',')
                      .append("\"jobRole\":").append(quote(d.getJobRole())).append(',')
                      .append("\"packageLpa\":").append(d.getPackageLpa()).append(',')
                      .append("\"location\":").append(quote(d.getLocation())).append(',')
                      .append("\"jobType\":").append(quote(d.getJobType().getLabel())).append(',')
                      .append("\"minCgpa\":").append(d.getMinCgpa()).append(',')
                      .append("\"maxBacklogs\":").append(d.getMaxBacklogs()).append(',')
                      .append("\"batchYear\":").append(d.getBatchYear()).append(',')
                      .append("\"status\":").append(quote(d.getStatus().name())).append(',')
                      .append("\"lastDate\":").append(quote(d.getLastDate() != null ? d.getLastDate().toString() : "")).append(',')
                      .append("\"branches\":").append(quote(String.join(", ", d.getAllowedBranches())))
                      .append(",\"eligible\":").append(elig == null ? "true" : elig.isEligible())
                      .append(",\"eligibilityReason\":").append(quote(elig == null ? "N/A" : elig.getReason()))
                      .append('}');
                }
                sendJson(ex, 200, sb.append(']').toString());
            } else if ("POST".equalsIgnoreCase(method)) {
                Map<String, String> b = parseJson(readBody(ex));
                Drive d = new Drive();
                d.setCompanyId(Integer.parseInt(b.get("companyId")));
                d.setJobRole(b.get("jobRole"));
                d.setPackageLpa(Double.parseDouble(b.get("packageLpa")));
                d.setLocation(b.get("location"));
                d.setJobType(JobType.valueOf(b.getOrDefault("jobType", "FULL_TIME")));
                d.setStatus(DriveStatus.valueOf(b.getOrDefault("status", "OPEN")));
                d.setLastDate(LocalDate.parse(b.get("lastDate")));
                d.setDriveDate(LocalDate.parse(b.get("driveDate")));
                d.setMinCgpa(Double.parseDouble(b.getOrDefault("minCgpa", "6.0")));
                d.setMaxBacklogs(Integer.parseInt(b.getOrDefault("maxBacklogs", "0")));
                d.setBatchYear(Integer.parseInt(b.getOrDefault("batchYear", "2026")));
                d.setDescription(b.getOrDefault("description", ""));
                Set<String> br = new HashSet<>(Arrays.asList(b.getOrDefault("branches", "CSE,ECE").split(",")));
                br.removeIf(String::isBlank);
                d.setAllowedBranches(br);
                int actorId = Integer.parseInt(b.getOrDefault("actorUserId", "1"));
                Drive saved = driveService.createDrive(d, actorId);
                sendJson(ex, 200, "{\"id\":" + saved.getId() + "}");
            } else {
                sendJson(ex, 405, "{\"error\":\"Method Not Allowed\"}");
            }
        } catch (Exception e) {
            sendError(ex, 400, e);
        }
    }

    private void handleStudents(HttpExchange ex) throws IOException {
        try {
            Map<String, String> q = parseQuery(ex.getRequestURI().getRawQuery());
            List<Student> list = studentService.searchStudents(
                    q.get("branch"),
                    Double.parseDouble(q.getOrDefault("minCgpa", "0")),
                    q.get("keyword"),
                    q.getOrDefault("sort", "CGPA_DESC"));
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append(',');
                sb.append(studentToJson(list.get(i)));
            }
            sendJson(ex, 200, sb.append(']').toString());
        } catch (Exception e) {
            sendError(ex, 400, e);
        }
    }

    private void handleStudentBlock(HttpExchange ex) throws IOException {
        if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
            sendJson(ex, 405, "{\"error\":\"POST required\"}");
            return;
        }
        try {
            Map<String, String> b = parseJson(readBody(ex));
            int studentId = Integer.parseInt(b.get("studentId"));
            boolean blocked = Boolean.parseBoolean(b.get("blocked"));
            int actorId = Integer.parseInt(b.getOrDefault("actorUserId", "1"));
            studentService.setBlocked(studentId, blocked, actorId);
            sendJson(ex, 200, "{\"blocked\":" + blocked + "}");
        } catch (Exception e) {
            sendError(ex, 400, e);
        }
    }

    private void handleApplications(HttpExchange ex) throws IOException {
        try {
            String method = ex.getRequestMethod();
            if ("GET".equalsIgnoreCase(method)) {
                Map<String, String> q = parseQuery(ex.getRequestURI().getRawQuery());
                List<Application> apps;
                if (q.containsKey("studentId")) {
                    apps = applicationService.getApplicationsByStudent(Integer.parseInt(q.get("studentId")));
                } else if (q.containsKey("driveId")) {
                    apps = applicationService.getApplicationsByDrive(Integer.parseInt(q.get("driveId")));
                } else {
                    apps = List.of();
                }
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < apps.size(); i++) {
                    if (i > 0) sb.append(',');
                    Application a = apps.get(i);
                    sb.append('{')
                      .append("\"id\":").append(a.getId()).append(',')
                      .append("\"studentId\":").append(a.getStudentId()).append(',')
                      .append("\"driveId\":").append(a.getDriveId()).append(',')
                      .append("\"studentRollNo\":").append(quote(a.getStudentRollNo())).append(',')
                      .append("\"studentName\":").append(quote(a.getStudentName())).append(',')
                      .append("\"studentBranch\":").append(quote(a.getStudentBranch())).append(',')
                      .append("\"studentCgpa\":").append(a.getStudentCgpa()).append(',')
                      .append("\"companyName\":").append(quote(a.getCompanyName())).append(',')
                      .append("\"jobRole\":").append(quote(a.getJobRole())).append(',')
                      .append("\"packageLpa\":").append(a.getPackageLpa()).append(',')
                      .append("\"status\":").append(quote(a.getStatus().name()))
                      .append('}');
                }
                sendJson(ex, 200, sb.append(']').toString());
            } else if ("POST".equalsIgnoreCase(method)) {
                Map<String, String> b = parseJson(readBody(ex));
                int studentId = Integer.parseInt(b.get("studentId"));
                int driveId = Integer.parseInt(b.get("driveId"));
                Student student = studentService.getById(studentId)
                        .orElseThrow(() -> new IllegalArgumentException("Student not found"));
                Application app = applicationService.apply(student, driveId);
                sendJson(ex, 200, "{\"id\":" + app.getId() + ",\"status\":\"APPLIED\"}");
            } else {
                sendJson(ex, 405, "{\"error\":\"Method Not Allowed\"}");
            }
        } catch (Exception e) {
            sendError(ex, 400, e);
        }
    }

    private void handleApplicationStage(HttpExchange ex) throws IOException {
        if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
            sendJson(ex, 405, "{\"error\":\"POST required\"}");
            return;
        }
        try {
            Map<String, String> b = parseJson(readBody(ex));
            int appId = Integer.parseInt(b.get("applicationId"));
            ApplicationStatus status = ApplicationStatus.valueOf(b.get("status"));
            int actorId = Integer.parseInt(b.getOrDefault("actorUserId", "1"));
            String remarks = b.getOrDefault("remarks", "");
            applicationService.updateStage(appId, status, actorId, remarks, LocalDate.now().plusMonths(6));
            sendJson(ex, 200, "{\"status\":" + quote(status.name()) + "}");
        } catch (Exception e) {
            sendError(ex, 400, e);
        }
    }

    private void handleApplicationHistory(HttpExchange ex) throws IOException {
        try {
            Map<String, String> q = parseQuery(ex.getRequestURI().getRawQuery());
            int appId = Integer.parseInt(q.get("id"));
            List<ApplicationStatusHistory> hist = applicationService.getStatusHistory(appId);
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < hist.size(); i++) {
                if (i > 0) sb.append(',');
                ApplicationStatusHistory h = hist.get(i);
                sb.append('{')
                  .append("\"from\":").append(quote(h.getFromStatus() != null ? h.getFromStatus().name() : "START")).append(',')
                  .append("\"to\":").append(quote(h.getToStatus().name())).append(',')
                  .append("\"remarks\":").append(quote(h.getRemarks())).append(',')
                  .append("\"changedAt\":").append(quote(h.getChangedAt() != null ? h.getChangedAt().toString() : ""))
                  .append('}');
            }
            sendJson(ex, 200, sb.append(']').toString());
        } catch (Exception e) {
            sendError(ex, 400, e);
        }
    }

    private void handleOffers(HttpExchange ex) throws IOException {
        try {
            Map<String, String> q = parseQuery(ex.getRequestURI().getRawQuery());
            int studentId = Integer.parseInt(q.get("studentId"));
            List<Offer> offers = offerService.getOffersForStudent(studentId);
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < offers.size(); i++) {
                if (i > 0) sb.append(',');
                Offer o = offers.get(i);
                sb.append('{')
                  .append("\"id\":").append(o.getId()).append(',')
                  .append("\"companyName\":").append(quote(o.getCompanyName())).append(',')
                  .append("\"jobRole\":").append(quote(o.getJobRole())).append(',')
                  .append("\"packageLpa\":").append(o.getPackageLpa()).append(',')
                  .append("\"joiningDate\":").append(quote(o.getJoiningDate() != null ? o.getJoiningDate().toString() : "")).append(',')
                  .append("\"status\":").append(quote(o.getStatus().name()))
                  .append('}');
            }
            sendJson(ex, 200, sb.append(']').toString());
        } catch (Exception e) {
            sendError(ex, 400, e);
        }
    }

    private void handleOfferRespond(HttpExchange ex) throws IOException {
        if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
            sendJson(ex, 405, "{\"error\":\"POST required\"}");
            return;
        }
        try {
            Map<String, String> b = parseJson(readBody(ex));
            int offerId = Integer.parseInt(b.get("offerId"));
            int userId = Integer.parseInt(b.get("userId"));
            boolean accept = Boolean.parseBoolean(b.get("accept"));
            offerService.respondToOffer(offerId, userId, accept);
            sendJson(ex, 200, "{\"status\":" + quote(accept ? "ACCEPTED" : "DECLINED") + "}");
        } catch (Exception e) {
            sendError(ex, 400, e);
        }
    }

    private void handleNotifications(HttpExchange ex) throws IOException {
        try {
            Map<String, String> q = parseQuery(ex.getRequestURI().getRawQuery());
            int userId = Integer.parseInt(q.get("userId"));
            List<Notification> list = notificationService.getForUser(userId);
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append(',');
                Notification n = list.get(i);
                sb.append('{')
                  .append("\"id\":").append(n.getId()).append(',')
                  .append("\"title\":").append(quote(n.getTitle())).append(',')
                  .append("\"message\":").append(quote(n.getMessage())).append(',')
                  .append("\"read\":").append(n.isRead())
                  .append('}');
            }
            sendJson(ex, 200, sb.append(']').toString());
        } catch (Exception e) {
            sendError(ex, 400, e);
        }
    }

    private void handleNotificationRead(HttpExchange ex) throws IOException {
        if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
            sendJson(ex, 405, "{\"error\":\"POST required\"}");
            return;
        }
        try {
            Map<String, String> b = parseJson(readBody(ex));
            notificationService.markAsRead(Integer.parseInt(b.get("id")));
            sendJson(ex, 200, "{\"read\":true}");
        } catch (Exception e) {
            sendError(ex, 400, e);
        }
    }

    private static String studentToJson(Student s) {
        return "{"
                + "\"id\":" + s.getId() + ","
                + "\"userId\":" + s.getUserId() + ","
                + "\"rollNo\":" + quote(s.getRollNo()) + ","
                + "\"fullName\":" + quote(s.getFullName()) + ","
                + "\"email\":" + quote(s.getEmail()) + ","
                + "\"branch\":" + quote(s.getBranch()) + ","
                + "\"batchYear\":" + s.getBatchYear() + ","
                + "\"cgpa\":" + s.getCgpa() + ","
                + "\"activeBacklogs\":" + s.getActiveBacklogs() + ","
                + "\"skills\":" + quote(s.getSkills()) + ","
                + "\"blocked\":" + s.isBlocked()
                + "}";
    }

    private static void sendJson(HttpExchange ex, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void sendError(HttpExchange ex, int status, Exception e) throws IOException {
        LOGGER.log(Level.WARNING, "REST error: " + e.getMessage());
        sendJson(ex, status, "{\"error\":" + quote(e.getMessage()) + "}");
    }

    private static String readBody(HttpExchange ex) throws IOException {
        try (InputStream in = ex.getRequestBody()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static Map<String, String> parseQuery(String raw) {
        Map<String, String> map = new HashMap<>();
        if (raw == null || raw.isBlank()) return map;
        for (String pair : raw.split("&")) {
            int idx = pair.indexOf('=');
            if (idx > 0) {
                String k = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                String v = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                map.put(k, v);
            }
        }
        return map;
    }

    /**
     * Minimal zero-dependency flat JSON object parser for simple key/value payloads.
     */
    private static Map<String, String> parseJson(String json) {
        Map<String, String> map = new HashMap<>();
        if (json == null) return map;
        String trimmed = json.trim();
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            trimmed = trimmed.substring(1, trimmed.length() - 1).trim();
        }
        if (trimmed.isEmpty()) return map;

        List<String> pairs = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            if (c == '"' && (i == 0 || trimmed.charAt(i - 1) != '\\')) {
                inQuotes = !inQuotes;
                cur.append(c);
            } else if (c == ',' && !inQuotes) {
                pairs.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        if (!cur.isEmpty()) pairs.add(cur.toString());

        for (String p : pairs) {
            int colon = p.indexOf(':');
            if (colon > 0) {
                String k = unquote(p.substring(0, colon).trim());
                String v = unquote(p.substring(colon + 1).trim());
                map.put(k, v);
            }
        }
        return map;
    }

    private static String unquote(String s) {
        if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
            s = s.substring(1, s.length() - 1);
        }
        return s.replace("\\\"", "\"").replace("\\n", "\n").replace("\\\\", "\\");
    }

    private static String mapToJson(Map<String, Integer> map) {
        StringBuilder sb = new StringBuilder("{");
        int i = 0;
        for (Map.Entry<String, Integer> e : map.entrySet()) {
            if (i++ > 0) sb.append(',');
            sb.append(quote(e.getKey())).append(':').append(e.getValue());
        }
        return sb.append('}').toString();
    }

    private static String quote(String s) {
        if (s == null) return "null";
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\"";
    }

    public static void main(String[] args) throws Exception {
        LoggerUtil.init();
        DatabaseInitializer.initialize();
        int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        PlacementRestServer server = new PlacementRestServer(port);
        server.start();
    }
}
