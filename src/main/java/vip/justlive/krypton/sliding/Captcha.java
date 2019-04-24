package vip.justlive.krypton.sliding;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 滑动图片验证码
 *
 * @author wubo
 */
@Data
@Accessors(chain = true)
public class Captcha {

  private String token;
  private String background;
  private String jigsaw;
  private int validate;
  @JSONField(serialize = false)
  private String kid;
  @JSONField(serialize = false)
  private String formatName;
  @JSONField(serialize = false)
  private int offset;
}
