package vip.justlive.krypton.controller;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import vip.justlive.krypton.sliding.Captcha;
import vip.justlive.krypton.sliding.Producer;
import vip.justlive.oxygen.core.domain.Resp;
import vip.justlive.oxygen.core.util.ExpiringMap;

/**
 * @author wubo
 */
@CrossOrigin
@RestController
@RequestMapping("sliding")
public class ImageController {

  private final Producer producer;

  public ImageController(@Value("${sliding.path}") String path) {
    this.producer = new Producer(path);
  }

  private ExpiringMap<String, Captcha> cache = ExpiringMap.<String, Captcha>builder()
      .expiration(5, TimeUnit.MINUTES)
      .build();

  @RequestMapping
  public ModelAndView view() {
    return new ModelAndView("index.html");
  }

  @RequestMapping("create")
  public Resp image() {
    Captcha captcha = producer.create();
    cache.put(captcha.getToken(), captcha);
    return Resp.success(captcha.getToken());
  }

  @RequestMapping("background")
  public void background(String token, HttpServletResponse response) throws IOException {
    Captcha captcha = cache.get(token);
    if (captcha != null) {
      ImageIO.write(captcha.getBackground(), captcha.getFormatName(), response.getOutputStream());
    }
  }

  @RequestMapping("front")
  public void front(String token, HttpServletResponse response) throws IOException {
    Captcha captcha = cache.get(token);
    if (captcha != null) {
      ImageIO.write(captcha.getFront(), captcha.getFormatName(), response.getOutputStream());
    }
  }

}
