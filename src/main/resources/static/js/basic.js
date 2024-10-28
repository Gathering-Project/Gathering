const host = 'http://' + window.location.host;
let targetId;
let folderTargetId;

$(document).ready(function () {
    const auth = getToken();

    if (auth !== undefined && auth !== '') {
        $.ajaxPrefilter(function (options, originalOptions, jqXHR) {
            jqXHR.setRequestHeader('Authorization', auth);
        });
    } else {
        window.location.href = host + '/api/user/login-page';
        return;
    }

    $.ajax({
        type: 'GET',
        url: `/api/user-info`,
        contentType: 'application/json',
    })
        .done(function (res, status, xhr) {
            const username = res.username;
            const isAdmin = !!res.admin;

            if (!username) {
                window.location.href = '/api/user/login-page';
                return;
            }

            $('#username').text(username);
            if (isAdmin) {
                $('#admin').text(true);
                showProduct();
            } else {
                showProduct();
            }

            // 로그인한 유저의 폴더
            $.ajax({
                type: 'GET',
                url: `/api/user-folder`,
                error(error) {
                    logout();
                }
            }).done(function (fragment) {
                $('#fragment').replaceWith(fragment);
            });

        })
        .fail(function (jqXHR, textStatus) {
            logout();
        });

    $('#query').on('keypress', function (e) {
        if (e.key == 'Enter') {
            execSearch();
        }
    });
    $('#close').on('click', function () {
        $('#container').removeClass('active');
    })
    $('#close2').on('click', function () {
        $('#container2').removeClass('active');
    })
    $('.nav div.nav-see').on('click', function () {
        $('div.nav-see').addClass('active');
        $('div.nav-search').removeClass('active');

        $('#see-area').show();
        $('#search-area').hide();
    })
    $('.nav div.nav-search').on('click', function () {
        $('div.nav-see').removeClass('active');
        $('div.nav-search').addClass('active');

        $('#see-area').hide();
        $('#search-area').show();
    })

    $('#see-area').show();
    $('#search-area').hide();
})


function logout() {
    // 토큰 삭제
    Cookies.remove('Authorization', {path: '/'});
    window.location.href = host + '/api/user/login-page';
}

function getToken() {

    let auth = Cookies.get('Authorization');

    if(auth === undefined) {
        return '';
    }

    // kakao 로그인 사용한 경우 Bearer 추가
    if(auth.indexOf('Bearer') === -1 && auth !== ''){
        auth = 'Bearer ' + auth;
    }

    return auth;
}
