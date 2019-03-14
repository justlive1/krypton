package vip.justlive.krypton.sliding;

import java.awt.image.BufferedImage;
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
  private BufferedImage background;
  private BufferedImage front;
}
