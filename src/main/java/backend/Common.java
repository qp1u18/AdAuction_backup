package backend;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This currently just contains all the enums we use, but could contain any
 * constants or code for use anywhere
 */
public class Common {

	public static <E extends Property> List<String> allValues(Class<E> en) {
		return Arrays.stream(en.getEnumConstants()).map(Property::getString).collect(Collectors.toList());
	}

	public interface Property {
		String getString();
	}


    public enum Gender implements Property {
        MALE("Male"),
        FEMALE("Female");

		private final String string;

		Gender(String string) {
			this.string = string;
		}


		public String getString() {
			return string;
		}
    }

    /**
     * U45: <25
     * F25T34: 25 - 34
     * F35T44: 35 - 44
     * F45T54: 45 - 54
     * O54: >54
     */
    public enum AgeRange  implements Property{
        U25("<25"), F25T34("25-34"), F35T44("35-44"), F45T54("45-54"), O54(">54");

		private final String string;

		AgeRange(String string) {
			this.string = string;
		}


		public String getString() {
			return string;
		}
    }

    public enum Income implements Property {
        LOW("Low"), MEDIUM("Medium"), HIGH("High");

		private final String string;

		Income(String string) {
			this.string = string;
		}


		public String getString() {
			return string;
		}
    }

    public enum Context implements Property {
        NEWS("News"), SHOPPING("Shopping"), SOCIAL_MEDIA("Social Media"), BLOG("Blog"), HOBBIES("Hobbies"), TRAVEL("Travel");

		private final String string;

		Context(String string) {
			this.string = string;
		}


		public String getString() {
			return string;
		}
    }

    public enum Interval implements Property {
        MINUTE("Minute"), HOUR("Hour"), DAY("Day"), WEEK("Week");

		private final String string;

		Interval(String string) {
        	this.string = string;
		}


		public String getString() {
			return string;
		}
	}

}
