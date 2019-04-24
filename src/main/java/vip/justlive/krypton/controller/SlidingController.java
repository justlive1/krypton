package vip.justlive.krypton.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import vip.justlive.krypton.sliding.Captcha;
import vip.justlive.krypton.sliding.Producer;
import vip.justlive.krypton.util.Encrypt;
import vip.justlive.oxygen.core.config.ConfigFactory;
import vip.justlive.oxygen.core.domain.Resp;
import vip.justlive.oxygen.core.template.SimpleTemplateEngine;
import vip.justlive.oxygen.core.template.TemplateEngine;
import vip.justlive.oxygen.core.template.Templates;
import vip.justlive.oxygen.core.util.ExpiringMap;
import vip.justlive.oxygen.web.annotation.Mapping;
import vip.justlive.oxygen.web.annotation.Param;
import vip.justlive.oxygen.web.annotation.Router;
import vip.justlive.oxygen.web.result.Result;
import vip.justlive.oxygen.web.router.RoutingContext;

/**
 * @author wubo
 */
@Router("sliding")
public class SlidingController {

  private final Producer producer;
  private final TemplateEngine engine;

  private ExpiringMap<String, String> KEYS = ExpiringMap.<String, String>builder()
      .expiration(10, TimeUnit.MINUTES).build();
  private ExpiringMap<String, Captcha> CAPTCHAS = ExpiringMap.<String, Captcha>builder()
      .expiration(5, TimeUnit.MINUTES).build();

  public SlidingController() {
    this.producer = new Producer(ConfigFactory.getProperty("sliding.path"));
    this.engine = new SimpleTemplateEngine();
  }

  /**
   * 测试页面
   *
   * @return
   */
  @Mapping("/")
  public Result view() {
    return Result.view("/index.htm");
  }

  /**
   * js
   *
   * @return
   */
  @Mapping("krypton.js")
  public void js(RoutingContext ctx) {
    String token = UUID.randomUUID().toString();
    String key = Encrypt.generateKey(token);
    Map<String, Object> map = new HashMap<>(4, 1);
    map.put("token", token);
    map.put("key", key);
    KEYS.put(token, key);
    ctx.response().setContentType("application/javascript");
    ctx.response().write(engine.render(Templates.cachedTemplate("/templates/krypton.js"), map));
  }

  /**
   * 生成验证码图片
   *
   * @param token
   * @return
   */
  @Mapping("create")
  public Resp create(@Param("token") String token) {
    Captcha captcha = producer.create().setKid(token);
    CAPTCHAS.put(captcha.getToken(), captcha);
    return Resp.success(captcha);
  }

  /**
   * 前端校验
   *
   * @param token
   * @param data
   * @return
   */
  @Mapping("validate")
  public Resp validate(@Param("token") String token, @Param("data") String data) {
    Captcha captcha = CAPTCHAS.get(token);
    if (captcha == null) {
      return Resp.error("failed");
    }
    String key = KEYS.get(captcha.getKid());
    if (key == null) {
      return Resp.error("failed");
    }
    String[] arr = data.split("/");
    if (arr.length == 2 && producer
        .validate(captcha, Encrypt.decode(key, arr[0]), Encrypt.decode(key, arr[1]))) {
      return Resp.success(Encrypt.encode(key, captcha.getValidate()));
    }
    return Resp.error("failed");
  }

  /**
   * 后端最终校验
   *
   * @param token
   * @param validate
   * @return
   */
  @Mapping("check")
  public Resp check(@Param("token") String token, @Param("validate") String validate) {
    Captcha captcha = CAPTCHAS.remove(token);
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
