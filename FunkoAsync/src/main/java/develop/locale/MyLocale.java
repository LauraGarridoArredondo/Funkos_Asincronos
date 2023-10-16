package develop.locale;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

/**
 * La clase MyLocale proporciona métodos estáticos para formatear fechas y valores monetarios
 * en el formato localizado de España (español - España).
 */
public class MyLocale {
    private static final Locale locale = new Locale("es","ES");

    /**
     * Convierte una fecha LocalDate en una representación de cadena en formato localizado de España.
     *
     * @param date La fecha LocalDate que se va a formatear.
     * @return Una cadena que representa la fecha en el formato localizado de España.
     */
    public static String toLocalDate(LocalDate date) {
        return date.format(
                DateTimeFormatter
                        .ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)
        );
    }

    /**
     * Convierte un valor double en una representación de cadena con formato de moneda localizado de España (español - España).
     *
     * @param money El valor monetario que se va a formatear.
     * @return Una cadena que representa el valor monetario en el formato de moneda localizado de España.
     */
    public static String toLocalMoney(double money) {
        return NumberFormat.getCurrencyInstance(locale).format(money);
    }

}