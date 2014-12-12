package expression;

import io.vertx.codetrans.LiteralExpressionTest;
import io.vertx.codetrans.annotations.CodeTranslate;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class LiteralNull {

  @CodeTranslate
  public void start() throws Exception {
    LiteralExpressionTest.result = null;
  }
}
