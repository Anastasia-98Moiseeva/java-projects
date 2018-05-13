/**
 * Не удалось распознать выражение
 *
 * @author Moiseeva Anastasia @alloky
 * @since 04.10.17
 */
public class ParsingException extends Exception {

  public ParsingException(String message) {
    super(message);
  }

  public ParsingException(String message, Throwable cause) {
    super(message, cause);
  }
}
