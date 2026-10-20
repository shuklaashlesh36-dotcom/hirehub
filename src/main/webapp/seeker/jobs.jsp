<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.hirehub.model.User" %>
<%@ page import="com.hirehub.model.Job" %>
<%@ page import="com.hirehub.model.JobStatus" %>
<%@ page import="com.hirehub.model.Resume" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser == null) {
        response.sendRedirect(request.getContextPath() + "/login?authRequired=true");
        return;
    }

    @SuppressWarnings("unchecked")
    List<Job> jobs = (List<Job>) request.getAttribute("jobs");
    @SuppressWarnings("unchecked")
    List<String> locations = (List<String>) request.getAttribute("locations");
    @SuppressWarnings("unchecked")
    Set<Long> appliedJobIds = (Set<Long>) request.getAttribute("appliedJobIds");
    Resume candidateResume = (Resume) request.getAttribute("candidateResume");

    String csrfToken = (String) request.getAttribute("csrfToken");
    String errorMessage = (String) request.getAttribute("errorMessage");
    String keyword = (String) request.getAttribute("keyword");
    String selectedLocation = (String) request.getAttribute("selectedLocation");
    String selectedStatus = (String) request.getAttribute("selectedStatus");

    if (keyword == null) keyword = "";
    if (selectedLocation == null) selectedLocation = "";
    if (selectedStatus == null) selectedStatus = "APPROVED";
    if (csrfToken == null) csrfToken = "";

    SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Browse &amp; Search Jobs - HireHub</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <script src="${pageContext.request.contextPath}/js/theme.js"></script>
    <style>
        .search-box {
            display: grid;
            grid-template-columns: 2fr 1fr 1fr auto;
            gap: 0.75rem;
            align-items: center;
        }
        @media (max-width: 850px) {
            .search-box {
                grid-template-columns: 1fr;
            }
        }
        .job-card {
            background: var(--bg-card);
            border: 1px solid var(--border-color);
            border-radius: var(--radius-md);
            padding: 1.5rem;
            margin-bottom: 1.25rem;
            transition: transform 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease;
            display: flex;
            flex-direction: column;
            gap: 0.85rem;
            box-shadow: var(--shadow-card);
        }
        .job-card:hover {
            transform: translateY(-2px);
            border-color: var(--border-glow);
            box-shadow: var(--shadow-hover);
        }
        .badge {
            display: inline-flex;
            align-items: center;
            gap: 0.3rem;
            padding: 0.25rem 0.65rem;
            border-radius: 9999px;
            font-size: 0.75rem;
            font-weight: 600;
        }
        .badge-location {
            background: var(--info-bg);
            color: var(--info-text);
            border: 1px solid var(--info-border);
        }
        .badge-type {
            background: rgba(168, 85, 247, 0.15);
            color: #a855f7;
            border: 1px solid rgba(168, 85, 247, 0.3);
        }
        [data-theme="light"] .badge-type {
            background: #faf5ff;
            color: #7e22ce;
            border: 1px solid #e9d5ff;
        }
        .badge-salary {
            background: var(--success-bg);
            color: var(--success-text);
            border: 1px solid var(--success-border);
        }
        .badge-status {
            background: var(--warning-bg);
            color: var(--warning-text);
            border: 1px solid var(--warning-border);
        }
        .badge-approved {
            background: var(--success-bg);
            color: var(--success-text);
            border: 1px solid var(--success-border);
        }
        .skill-tag {
            background: var(--bg-tag);
            color: var(--text-primary);
            border: 1px solid var(--border-color);
            padding: 0.2rem 0.55rem;
            border-radius: 6px;
            font-size: 0.75rem;
        }
        .modal-overlay {
            position: fixed;
            top: 0; left: 0; right: 0; bottom: 0;
            background: rgba(0, 0, 0, 0.65);
            backdrop-filter: blur(6px);
            display: flex;
            align-items: center;
            justify-content: center;
            z-index: 100;
            padding: 1.5rem;
        }
        .modal-card {
            background: var(--bg-card-solid);
            border: 1px solid var(--border-color);
            border-radius: var(--radius-lg);
            max-width: 650px;
            width: 100%;
            max-height: 90vh;
            overflow-y: auto;
            padding: 2rem;
            box-shadow: var(--shadow-modal);
        }
    </style>
</head>
<body>
    <nav class="navbar">
        <a href="${pageContext.request.contextPath}/seeker/dashboard.jsp" class="brand">
            HireHub <span class="brand-badge" style="background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);">Candidate</span>
        </a>
        <div style="display: flex; align-items: center; gap: 0.75rem; flex-wrap: wrap;">
            <button type="button" class="theme-toggle" aria-label="Toggle theme" title="Toggle Light/Dark Theme">
                <span class="theme-toggle-icon" aria-hidden="true">&#9728;&#65039;</span>
                <span class="theme-toggle-text">Theme</span>
            </button>
            <a href="${pageContext.request.contextPath}/seeker/dashboard.jsp" class="btn btn-secondary" style="padding: 0.45rem 0.85rem; font-size: 0.85rem;">
                &#128100; Dashboard
            </a>
            <a href="${pageContext.request.contextPath}/seeker/applications" class="btn btn-secondary" style="padding: 0.45rem 0.85rem; font-size: 0.85rem;">
                &#128196; My Applications
            </a>
            <a href="${pageContext.request.contextPath}/seeker/resume" class="btn btn-secondary" style="padding: 0.45rem 0.85rem; font-size: 0.85rem;">
                &#128193; Resume
            </a>
            <a href="${pageContext.request.contextPath}/profile" class="user-badge" style="text-decoration: none; display: flex; align-items: center; gap: 0.5rem;" title="View Profile">
                <img src="${pageContext.request.contextPath}/profile/photo" alt="Avatar" class="avatar-nav">
                <span><strong><%= currentUser.getName() %></strong></span>
            </a>
            <a href="${pageContext.request.contextPath}/logout" class="btn btn-secondary" style="padding: 0.45rem 0.85rem; font-size: 0.85rem;">
                Sign Out
            </a>
        </div>
    </nav>

    <div class="container" style="max-width: 1050px; margin-top: 2rem;">
        
        <!-- Header -->
        <div style="margin-bottom: 2rem;">
            <h1 style="font-size: 2rem; font-weight: 800; margin-bottom: 0.5rem; color: var(--text-primary);">
                Find Your Next Career Move
            </h1>
            <p style="color: var(--text-secondary); font-size: 1rem;">
                Search verified job opportunities posted by registered employers on HireHub.
            </p>
        </div>

        <!-- Error Alert -->
        <% if (errorMessage != null && !errorMessage.isBlank()) { %>
            <div class="alert alert-error" style="margin-bottom: 1.5rem;">
                &#9888; <%= errorMessage %>
            </div>
        <% } %>

        <!-- Search & Filter Controls -->
        <div class="glass-panel" style="padding: 1.5rem; margin-bottom: 2rem;">
            <form action="${pageContext.request.contextPath}/seeker/jobs" method="get">
                <div class="search-box">
                    <div>
                        <input type="text" name="keyword" class="form-control" 
                               placeholder="Search title, skills (e.g. Java, SQL), company..."
                               value="<%= keyword %>">
                    </div>
                    <div>
                        <select name="location" class="form-control" style="cursor: pointer;">
                            <option value="">All Locations</option>
                            <% if (locations != null) {
                                for (String loc : locations) { %>
                                    <option value="<%= loc %>" <%= loc.equalsIgnoreCase(selectedLocation) ? "selected" : "" %>>
                                        <%= loc %>
                                    </option>
                            <%  }
                               } %>
                        </select>
                    </div>
                    <div>
                        <select name="status" class="form-control" style="cursor: pointer;">
                            <option value="APPROVED" <%= "APPROVED".equalsIgnoreCase(selectedStatus) ? "selected" : "" %>>Approved Openings</option>
                            <option value="ALL" <%= "ALL".equalsIgnoreCase(selectedStatus) ? "selected" : "" %>>All Listings</option>
                        </select>
                    </div>
                    <div style="display: flex; gap: 0.5rem;">
                        <button type="submit" class="btn btn-primary" style="padding: 0.65rem 1.25rem;">
                            Search
                        </button>
                        <% if (!keyword.isBlank() || !selectedLocation.isBlank() || !"APPROVED".equalsIgnoreCase(selectedStatus)) { %>
                            <a href="${pageContext.request.contextPath}/seeker/jobs" class="btn btn-secondary" style="padding: 0.65rem 1rem;" title="Reset filters">
                                Reset
                            </a>
                        <% } %>
                    </div>
                </div>
            </form>
        </div>

        <!-- Results Counter -->
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.25rem;">
            <div style="font-size: 0.95rem; color: var(--text-secondary);">
                Showing <strong><%= (jobs != null) ? jobs.size() : 0 %></strong> job openings
                <% if (!keyword.isBlank()) { %>
                    matching "<em><%= keyword %></em>"
                <% } %>
                <% if (!selectedLocation.isBlank()) { %>
                    in <strong><%= selectedLocation %></strong>
                <% } %>
            </div>
            <div style="font-size: 0.85rem; color: var(--text-muted);">
                Updated in real-time from MySQL
            </div>
        </div>

        <!-- Job Listings -->
        <% if (jobs == null || jobs.isEmpty()) { %>
            <div class="glass-panel" style="text-align: center; padding: 4rem 2rem; margin-top: 1rem;">
                <div style="font-size: 3rem; margin-bottom: 1rem;">&#128269;</div>
                <h3 style="font-size: 1.35rem; font-weight: 700; margin-bottom: 0.5rem;">No Job Openings Found</h3>
                <p style="color: var(--text-secondary); max-width: 480px; margin: 0 auto 1.5rem auto;">
                    We couldn't find any job postings matching your current criteria. Try broadening your keyword search or clearing your location filter.
                </p>
                <a href="${pageContext.request.contextPath}/seeker/jobs" class="btn btn-secondary">
                    Clear Search Filters
                </a>
            </div>
        <% } else { %>
            <div style="display: flex; flex-direction: column;">
                <% for (Job job : jobs) { 
                    boolean alreadyApplied = (appliedJobIds != null && appliedJobIds.contains(job.getId()));
                %>
                    <div class="job-card">
                        <div style="display: flex; justify-content: space-between; align-items: flex-start; gap: 1rem; flex-wrap: wrap;">
                            <div>
                                <h2 style="font-size: 1.35rem; font-weight: 700; margin-bottom: 0.35rem; color: var(--text-primary);">
                                    <%= job.getTitle() %>
                                </h2>
                                <div style="font-size: 0.95rem; color: var(--accent-primary); font-weight: 600; display: flex; align-items: center; gap: 0.4rem;">
                                    <span>&#127970;</span> <%= job.getCompanyName() %>
                                </div>
                            </div>
                            <div style="display: flex; gap: 0.5rem; flex-wrap: wrap; align-items: center;">
                                <% if (job.getLocation() != null && !job.getLocation().isBlank()) { %>
                                    <span class="badge badge-location">&#128205; <%= job.getLocation() %></span>
                                <% } %>
                                <span class="badge badge-type">&#128188; <%= job.getJobType() %></span>
                                <% if (job.getSalary() != null && !job.getSalary().isBlank()) { %>
                                    <span class="badge badge-salary">&#128181; <%= job.getSalary() %></span>
                                <% } %>
                                <% if (job.getStatus() == JobStatus.APPROVED) { %>
                                    <span class="badge badge-approved">&#10003; Approved</span>
                                <% } else { %>
                                    <span class="badge badge-status">&#9203; <%= job.getStatus() %></span>
                                <% } %>
                            </div>
                        </div>

                        <!-- Description Snippet -->
                        <% if (job.getDescription() != null && !job.getDescription().isBlank()) { 
                            String desc = job.getDescription();
                            String snippet = desc.length() > 220 ? desc.substring(0, 217) + "..." : desc;
                        %>
                            <p style="color: var(--text-secondary); font-size: 0.92rem; line-height: 1.55;">
                                <%= snippet %>
                            </p>
                        <% } %>

                        <!-- Skills Chips -->
                        <% if (job.getSkills() != null && !job.getSkills().isBlank()) { %>
                            <div style="display: flex; gap: 0.4rem; flex-wrap: wrap; align-items: center;">
                                <span style="font-size: 0.8rem; color: var(--text-muted); margin-right: 0.2rem;">Skills:</span>
                                <% String[] skillsArr = job.getSkills().split(",");
                                   for (String skill : skillsArr) { 
                                       String trimmed = skill.trim();
                                       if (!trimmed.isBlank()) { %>
                                           <span class="skill-tag"><%= trimmed %></span>
                                <%     }
                                   } %>
                            </div>
                        <% } %>

                        <!-- Card Footer -->
                        <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 0.5rem; border-top: 1px solid var(--border-color); padding-top: 0.85rem; flex-wrap: wrap; gap: 0.75rem;">
                            <div style="font-size: 0.8rem; color: var(--text-muted);">
                                Posted: <%= (job.getCreatedAt() != null) ? dateFormat.format(job.getCreatedAt()) : "Recently" %>
                            </div>
                            <div style="display: flex; gap: 0.75rem; align-items: center;">
                                <button type="button" class="btn btn-secondary" style="padding: 0.45rem 1rem; font-size: 0.85rem;"
                                        onclick="openJobModal('<%= job.getId() %>', '<%= job.getTitle().replace("'", "\\'") %>', '<%= job.getCompanyName().replace("'", "\\'") %>', '<%= (job.getLocation() != null ? job.getLocation().replace("'", "\\'") : "") %>', '<%= (job.getSalary() != null ? job.getSalary().replace("'", "\\'") : "Undisclosed") %>', '<%= (job.getSkills() != null ? job.getSkills().replace("'", "\\'") : "") %>', `<%= (job.getDescription() != null ? job.getDescription().replace("`", "\\`") : "") %>`)">
                                    View Details
                                </button>
                                
                                <% if (alreadyApplied) { %>
                                    <span class="badge badge-approved" style="padding: 0.45rem 1rem; font-size: 0.85rem;">
                                        &#10004; Applied
                                    </span>
                                <% } else { %>
                                    <button type="button" class="btn btn-primary" style="padding: 0.45rem 1.15rem; font-size: 0.85rem;"
                                            onclick="openApplyModal('<%= job.getId() %>', '<%= job.getTitle().replace("'", "\\'") %>', '<%= job.getCompanyName().replace("'", "\\'") %>')">
                                        &#128640; Apply Now
                                    </button>
                                <% } %>
                            </div>
                        </div>
                    </div>
                <% } %>
            </div>
        <% } %>
    </div>

    <!-- Apply Confirmation Modal -->
    <div id="applyModal" class="modal-overlay" style="display: none;" onclick="closeApplyModal(event)">
        <div class="modal-card" onclick="event.stopPropagation()" style="max-width: 520px;">
            <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 1.25rem;">
                <div>
                    <h2 id="applyModalTitle" style="font-size: 1.35rem; font-weight: 800; color: var(--text-primary); margin-bottom: 0.25rem;"></h2>
                    <p id="applyModalCompany" style="color: var(--accent-primary); font-weight: 600; font-size: 0.95rem;"></p>
                </div>
                <button type="button" onclick="closeApplyModal()" style="background: none; border: none; color: var(--text-muted); font-size: 1.5rem; cursor: pointer;">&times;</button>
            </div>
            
            <div class="glass-panel" style="padding: 1rem 1.25rem; margin-bottom: 1.25rem; border-color: rgba(99, 102, 241, 0.3);">
                <% if (candidateResume != null) { %>
                    <div style="display: flex; align-items: center; gap: 0.65rem;">
                        <span style="font-size: 1.25rem;">&#128196;</span>
                        <div>
                            <div style="font-size: 0.8rem; color: var(--success-text); font-weight: 600;">Resume Attached</div>
                            <div style="font-size: 0.9rem; color: var(--text-primary);"><%= candidateResume.getFileName() %></div>
                        </div>
                    </div>
                <% } else { %>
                    <div style="font-size: 0.85rem; color: var(--warning-text); line-height: 1.5;">
                        &#9888; You have not uploaded a resume yet. You can submit now using your profile information, or <a href="${pageContext.request.contextPath}/seeker/resume" style="color: var(--accent-primary); text-decoration: underline;">upload a PDF resume</a> first.
                    </div>
                <% } %>
            </div>

            <form action="${pageContext.request.contextPath}/seeker/apply" method="post">
                <input type="hidden" name="csrfToken" value="<%= csrfToken %>">
                <input type="hidden" name="jobId" id="applyJobIdInput" value="">
                
                <p style="color: var(--text-secondary); font-size: 0.9rem; margin-bottom: 1.25rem;">
                    Ready to submit your application? The employer will review your profile and contact details.
                </p>

                <div style="display: flex; justify-content: flex-end; gap: 0.75rem;">
                    <button type="button" class="btn btn-secondary" onclick="closeApplyModal()">Cancel</button>
                    <button type="submit" class="btn btn-primary" style="padding: 0.6rem 1.5rem;">
                        &#128640; Submit Application
                    </button>
                </div>
            </form>
        </div>
    </div>

    <!-- Job Details Modal -->
    <div id="jobModal" class="modal-overlay" style="display: none;" onclick="closeJobModal(event)">
        <div class="modal-card" onclick="event.stopPropagation()">
            <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 1.25rem;">
                <div>
                    <h2 id="modalTitle" style="font-size: 1.5rem; font-weight: 800; color: var(--text-primary); margin-bottom: 0.25rem;"></h2>
                    <p id="modalCompany" style="color: var(--accent-primary); font-weight: 600; font-size: 1rem;"></p>
                </div>
                <button type="button" onclick="closeJobModal()" style="background: none; border: none; color: var(--text-muted); font-size: 1.5rem; cursor: pointer;">&times;</button>
            </div>
            
            <div id="modalBadges" style="display: flex; gap: 0.5rem; flex-wrap: wrap; margin-bottom: 1.25rem;"></div>

            <div style="margin-bottom: 1.25rem;">
                <h4 style="font-size: 0.9rem; text-transform: uppercase; letter-spacing: 0.05em; color: var(--text-muted); margin-bottom: 0.5rem;">Required Skills</h4>
                <div id="modalSkills" style="display: flex; gap: 0.4rem; flex-wrap: wrap;"></div>
            </div>

            <div style="margin-bottom: 1.5rem;">
                <h4 style="font-size: 0.9rem; text-transform: uppercase; letter-spacing: 0.05em; color: var(--text-muted); margin-bottom: 0.5rem;">Job Description</h4>
                <div id="modalDescription" style="color: var(--text-secondary); line-height: 1.7; font-size: 0.95rem; white-space: pre-wrap;"></div>
            </div>

            <div style="display: flex; justify-content: flex-end; gap: 0.75rem; border-top: 1px solid var(--border-color); padding-top: 1rem;">
                <button type="button" class="btn btn-secondary" onclick="closeJobModal()">Close</button>
            </div>
        </div>
    </div>

    <footer class="footer" style="margin-top: 3rem;">
        HireHub &copy; 2026. Online Job Portal.
    </footer>

    <script>
        function openJobModal(id, title, company, location, salary, skills, description) {
            document.getElementById('modalTitle').textContent = title;
            document.getElementById('modalCompany').textContent = company;
            
            const badgesDiv = document.getElementById('modalBadges');
            badgesDiv.innerHTML = '';
            if (location) {
                badgesDiv.innerHTML += '<span class="badge badge-location">&#128205; ' + escapeHtml(location) + '</span>';
            }
            badgesDiv.innerHTML += '<span class="badge badge-type">&#128188; Full-time</span>';
            if (salary && salary !== 'Undisclosed') {
                badgesDiv.innerHTML += '<span class="badge badge-salary">&#128181; ' + escapeHtml(salary) + '</span>';
            }

            const skillsDiv = document.getElementById('modalSkills');
            skillsDiv.innerHTML = '';
            if (skills) {
                skills.split(',').forEach(s => {
                    const t = s.trim();
                    if (t) {
                        skillsDiv.innerHTML += '<span class="skill-tag">' + escapeHtml(t) + '</span>';
                    }
                });
            } else {
                skillsDiv.innerHTML = '<span style="color: var(--text-muted); font-size: 0.85rem;">None specified</span>';
            }

            document.getElementById('modalDescription').textContent = description;
            document.getElementById('jobModal').style.display = 'flex';
        }

        function closeJobModal(e) {
            if (!e || e.target === document.getElementById('jobModal')) {
                document.getElementById('jobModal').style.display = 'none';
            }
        }

        function openApplyModal(jobId, title, company) {
            document.getElementById('applyJobIdInput').value = jobId;
            document.getElementById('applyModalTitle').textContent = 'Apply for ' + title;
            document.getElementById('applyModalCompany').textContent = company;
            document.getElementById('applyModal').style.display = 'flex';
        }

        function closeApplyModal(e) {
            if (!e || e.target === document.getElementById('applyModal')) {
                document.getElementById('applyModal').style.display = 'none';
            }
        }

        function escapeHtml(text) {
            const div = document.createElement('div');
            div.textContent = text;
            return div.innerHTML;
        }
    </script>
</body>
</html>
