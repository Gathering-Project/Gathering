package nbc_final.gathering.domain.user.utils;


import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GenerateRandomNickname {

    public static String generateNickname() {
        List<String> abj = Arrays.asList(
                "엉뚱한", "발랄한", "알쏭달쏭한 ", "시끌벅적한", "느끼한", "쫀득쫀득한", "번뜩이는", "오싹한", "보송보송한",
                "짜릿한", "비실비실한", "느긋한", "까칠한", "말랑말랑한", "쾌활한", "홀가분한", "반짝이는", "표독한", "눈 돌아간"
        );
        List<String> pings = Arrays.asList(
                "하츄핑", "바로핑", "아자핑", "차차핑", "라라핑", "해핑", "조아핑", "티니핑",
                "방글핑", "믿어핑", "꾸래핑", "나나핑", "솔찌핑", "말랑핑", "샤샤핑",
                "행운핑", "새콤핑", "달콤핑", "모야핑", "무셔핑", "화나핑", "시진핑"
        );

        Random random = new Random();
        int num = random.nextInt(10000);
        String randomNum = String.valueOf(num);

        Collections.shuffle(abj); // 형용사 리스트 섞기
        Collections.shuffle(pings); // 캐릭터 리스트 섞기

        return abj.get(0) + " " + pings.get(0) + " " + randomNum;
    }
}
