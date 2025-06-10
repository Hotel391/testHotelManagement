
<div class="top-navbar">
    <span id="menu-toggle"><i class="bi bi-list"></i></span>
    <h1>${title}</h1>
    <div class="user-profile">
        <img id="user-profile-button" src="${pageContext.request.contextPath}/Image/User.png" alt="User Profile">
        <div class="user-info">
            <a href="${pageContext.request.contextPath}/managerProfile" class="text-decoration-none text-reset">
                <i class="bi bi-person-circle"></i>Profile</a><br>
                <a href="${pageContext.request.contextPath}/login?service=logout" class="text-decoration-none text-reset">
                <i class="bi bi-box-arrow-right"></i>Logout</a>
        </div>
    </div>
</div>
