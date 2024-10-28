// login.js

const host = 'http://' + window.location.host;

$(document).ready(function () {
    // 카카오 로그인 URL 가져와서 리다이렉트 설정
    $.ajax({
        type: "GET",
        url: "/api/v1/auth/kakao-url",
        success: function(authUrl) {
            $(".kakao-btn").on("click", function() {
                window.location.href = authUrl;
            });
        },
        error: function() {
            alert("카카오 인증 URL을 불러오는 데 문제가 발생했습니다.");
        }
    });
});

// 카카오 콜백 요청 후 마이페이지로 리다이렉트
function kakaoCallback(code) {
    $.ajax({
        type: "GET",
        url: `/api/v1/users/kakao/callback?code=${code}`,
        success: function(response) {
            if (response.redirectUrl) {
                // 절대 경로로 리다이렉트 설정
                window.location.href = window.location.origin + response.redirectUrl;
            } else {
                alert("리다이렉트 URL이 없습니다.");
            }
        },
        error: function() {
            alert("카카오 로그인 중 오류가 발생했습니다.");
        }
    });
}
