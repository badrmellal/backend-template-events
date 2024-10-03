package backend.event_management_system.constant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Country {
    private String name;
    private String code;
    private Currency currency;

    public Country(String name, String code, Currency currency) {
        this.name = name;
        this.code = code;
        this.currency = currency;
    }

    @Setter
    @Getter
    public static class Currency {
        private String name;
        private String code;
        private String symbol;

        public Currency(String name, String code, String symbol) {
            this.name = name;
            this.code = code;
            this.symbol = symbol;
        }

    }
}
