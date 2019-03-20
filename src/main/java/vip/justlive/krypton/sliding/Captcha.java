package vip.justlive.krypton.sliding;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
  @JsonIgnore
  private String formatName;
  @JsonIgnore
  private int offset;
}
