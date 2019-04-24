package vip.justlive.krypton;

import vip.justlive.oxygen.web.server.Server;

/**
 * 启动类
 *
 * @author wubo
 */
public class Application {

  public static void main(String[] args) {
    Server.server().listen();
  }
}
