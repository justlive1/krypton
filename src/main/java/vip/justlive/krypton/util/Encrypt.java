package vip.justlive.krypton.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * 一个简单的加密数字算法<br>
 * 1.生成10位随机字符串<br>
 * 2.简单的字符替换生成密文
 * 
 * @author wubo
 *
 */
public class Encrypt {

  private static final List<String> DIST =
      Arrays.asList("1234567890qwertyuiopasdfghjklzxcvbnm".split(""));

  private static final Random RND = new Random();

  public static String generateKey(String token) {
    Collections.shuffle(DIST, RND);
    return DIST.subList(0, 10).stream().collect(Collectors.joining());
  }

  public static String encode(String key, int raw) {
    String[] arr = key.split("");
    StringBuilder sb = new StringBuilder();
    while (raw > 0) {
      sb.append(arr[raw % 10]);
      raw /= 10;
    }
    return sb.toString();
  }

  public static int decode(String key, String raw) {
    Map<String, Integer> map = new HashMap<>();
    String[] arr = key.split("");
    for (Integer index = 0; index < arr.length; index++) {
      map.put(arr[index], index);
    }
    int value = 0;
    arr = raw.split("");
    for (int i = 0; i < arr.length; i++) {
      value += Math.max(1, Math.pow(10, i)) * map.get(arr[i]);
    }
    return value;
  }
}
