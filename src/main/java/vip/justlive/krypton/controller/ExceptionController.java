package vip.justlive.krypton.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.domain.Resp;
import vip.justlive.oxygen.core.exception.CodedException;
import vip.justlive.oxygen.core.exception.ErrorCode;

/**
 * 全局异常处理
 * 
 * @author wubo
 *
 */
@Slf4j
@ControllerAdvice
public class ExceptionController {

  /**
   * 自定义异常
   */
  @ExceptionHandler(CodedException.class)
  @ResponseBody
  public Resp handleCodedException(CodedException e) {
    log.error(e.getMessage(), e);
    ErrorCode errorCode = e.getErrorCode();
    if (errorCode != null) {
      return Resp.error(errorCode.getCode(), errorCode.getMessage());
    }
    return Resp.error(e.getMessage());
  }

  @ExceptionHandler(Exception.class)
  @ResponseBody
  public Resp handleException(Exception e) {
    log.error(e.getMessage(), e);
    return Resp.error(e.getMessage());
  }

}
