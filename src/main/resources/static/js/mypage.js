$(document).ready(function () {
    const auth = getToken();

    if (!auth) {
        window.location.href = '/login.html';  // 인증이 안 된 경우 로그인 페이지로 리다이렉트
        return;
    }

    $.ajax({
        type: 'GET',
        url: '/api/user-info',
        headers: { 'Authorization': auth },
        success: function(res) {
            $('#username').text(res.username);
        },
        error: function() {
            logout();  // 인증 실패 시 로그아웃 처리
        }
    });
});

function logout() {
    Cookies.remove('Authorization', { path: '/' });
    window.location.href = '/login.html';
}

function getToken() {
    let auth = Cookies.get('Authorization');
    return auth ? `Bearer ${auth}` : '';
}
