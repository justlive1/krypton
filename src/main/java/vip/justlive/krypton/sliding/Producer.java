package vip.justlive.krypton.sliding;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.util.Random;
import java.util.UUID;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import vip.justlive.oxygen.core.constant.Constants;
import vip.justlive.oxygen.core.exception.Exceptions;

/**
 * 验证码生成器
 *
 * @author wubo
 */
public class Producer {

  private static final int DEFAULT_WIDTH = 480;
  private static final int DEFAULT_HEIGHT = 240;
  private static final int DEFAULT_WIDTH_OFFSET = 120;
  private static final int DEFAULT_FRONT_WIDTH = 60;

  private final Random random = new Random();
  /**
   * 图片路径
   */
  private final String path;
  private final int width;
  private final int height;
  private final int widthOffset;
  private final int frontWidth;

  public Producer(String path) {
    this(path, DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_WIDTH_OFFSET, DEFAULT_FRONT_WIDTH);
  }

  public Producer(String path, int width, int height, int widthOffset, int frontWidth) {
    File dir = new File(path);
    if (!dir.exists() || !dir.isDirectory()) {
      throw new IllegalArgumentException("图片目录不正确 " + path);
    }
    this.path = path;
    this.width = width;
    this.height = height;
    this.widthOffset = widthOffset;
    this.frontWidth = frontWidth;
  }

  /**
   * 生成验证码
   *
   * @return captcha
   */
  public Captcha create() {
    File dir = new File(path);
    File[] files = dir.listFiles();
    File file = files[random.nextInt(files.length)];

    int x = random.nextInt(width - frontWidth - widthOffset) + widthOffset;
    int y = random.nextInt(height - frontWidth);
    try {
      return new Captcha().setToken(UUID.randomUUID().toString()).setFront(front(file, x, y))
          .setBackground(background(file, x, y));
    } catch (Exception e) {
      throw Exceptions.wrap(e);
    }
  }

  private BufferedImage background(File file, int x, int y) throws Exception {
    Rectangle rectangle = new Rectangle(0, 0, width, height);
    try (FileInputStream fis = new FileInputStream(file); ImageInputStream iis = ImageIO
        .createImageInputStream(fis)) {
      ImageReader reader = ImageIO.getImageReadersBySuffix(
          file.getName().substring(file.getName().lastIndexOf(Constants.DOT) + 1)).next();
      reader.setInput(iis, true);
      ImageReadParam param = reader.getDefaultReadParam();
      param.setSourceRegion(rectangle);
      BufferedImage bi = reader.read(0, param);
      Graphics2D g = bi.createGraphics();
      g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
      g.setColor(Color.white);
      g.fillRect(x, y, frontWidth, frontWidth);
      g.dispose();
      return bi;
    }
  }

  private BufferedImage front(File file, int x, int y) throws Exception {
    Rectangle rectangle = new Rectangle(x, y, frontWidth, frontWidth);
    try (FileInputStream fis = new FileInputStream(file); ImageInputStream iis = ImageIO
        .createImageInputStream(fis)) {
      ImageReader reader = ImageIO.getImageReadersBySuffix(
          file.getName().substring(file.getName().lastIndexOf(Constants.DOT) + 1)).next();
      reader.setInput(iis, true);
      ImageReadParam param = reader.getDefaultReadParam();
      param.setSourceRegion(rectangle);
      return reader.read(0, param);
    }
  }
}
