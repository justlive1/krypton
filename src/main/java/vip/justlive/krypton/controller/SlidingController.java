package vip.justlive.krypton.controller;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import vip.justlive.krypton.sliding.Captcha;
import vip.justlive.krypton.sliding.Encrypt;
import vip.justlive.krypton.sliding.Producer;
import vip.justlive.oxygen.core.domain.Resp;
import vip.justlive.oxygen.core.util.ExpiringMap;

/**
 * @author wubo
 */
@CrossOrigin
@RestController
@RequestMapping("sliding")
public class SlidingController {

  private final Producer producer;

  private ExpiringMap<String, String> KEYS =
      ExpiringMap.<String, String>builder().expiration(10, TimeUnit.MINUTES).build();
  private ExpiringMap<String, Captcha> CAPTCHAS =
      ExpiringMap.<String, Captcha>builder().expiration(5, TimeUnit.MINUTES).build();

  public SlidingController(@Value("${sliding.path}") String path) {
    this.producer = new Producer(path);
  }

  @RequestMapping
  public ModelAndView view() {
    return new ModelAndView("index.html");
  }

  @RequestMapping("krypton.js")
  public ModelAndView js(Model model) {
    String token = UUID.randomUUID().toString();
    String key = Encrypt.generateKey(token);
    model.addAttribute("token", token);
    model.addAttribute("key", key);
    KEYS.put(token, key);
    return new ModelAndView("krypton.js");
  }

  @RequestMapping("create")
  public Resp create(@RequestParam String token) {
    Captcha captcha = producer.create().setKid(token);
    CAPTCHAS.put(captcha.getToken(), captcha);
    return Resp.success(captcha);
  }

  @RequestMapping("validate")
  public Resp validate(@RequestParam String token, @RequestParam String data) {
    Captcha captcha = CAPTCHAS.get(token);
    if (captcha == null) {
      return Resp.error("failed");
    }
    String key = KEYS.get(captcha.getKid());
    if (key == null) {
      return Resp.error("failed");
    }
    String[] arr = data.split("/");
    if (arr.length == 2
        && producer.validate(captcha, Encrypt.decode(key, arr[0]), Encrypt.decode(key, arr[1]))) {
      return Resp.success(Encrypt.encode(key, captcha.getValidate()));
    }
    return Resp.error("failed");
  }

  @RequestMapping("check")
  public Resp check(@RequestParam String token, @RequestParam String validate) {
    Captcha captcha = CAPTCHAS.get(token);
    if (captcha == null) {
      return Resp.error("failed");
    }
    String key = KEYS.get(captcha.getKid());
    if (key == null) {
      return Resp.error("failed");
    }
    if (captcha.getValidate() != Encrypt.decode(key, validate)) {
      return Resp.error("failed");
    }
    return Resp.success();
  }
}
