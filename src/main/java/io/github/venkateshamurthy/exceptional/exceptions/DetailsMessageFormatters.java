package io.github.venkateshamurthy.exceptional.exceptions;

import io.vavr.control.Try;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.slf4j.helpers.MessageFormatter;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * An enumeration of Message formatters that can be used to format the detail message.
 */
@Getter
@Slf4j
public enum DetailsMessageFormatters {
    /** A no/none formatting enum to be used as a catch-all default if no other pattern works.*/
    NONE(""){
        /**{@inheritDoc}.<b>No formatter is applied and hence return template as it is.</b>*/
        public String format(String template, Object... values) {
            //validate(template);
            return template;
        }
    },

    /** A SLF4J logger style empty braces pattern.*/
    SLF4J("\\{}") {
        /**{@inheritDoc}. A standard SLF4J style formatter with just the empty braces {} for value replacement.*/
        public String format(String template, Object... values) {
            //validate(template);
            return MessageFormatter.basicArrayFormat(template, values);
        }
    },

    /** A double angular braces &lt;&lt;&gt;&gt; marker.*/
    DOUBLEANGULAR("\\<<(?=[a-zA-Z0-9]*[a-zA-Z])[a-zA-Z0-9]+\\>>") {
        /**{@inheritDoc}. A double angular braces &lt;&lt;somekey&gt;&gt; style pattern for replacing values.*/
        @Override
        public String format(String template, Object... values) {
            // If the values is just a Map (of key,value) then replace/substitute each key with mapping value from map
            if (values.length == 1 && values[0] instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) values[0];
                return StringSubstitutor.replace(template, map, "<<", ">>");
            }

            //In case if the values are in a var-args style such as value-1, value-2,....
            final String[] keys = StringUtils.substringsBetween(template, "<<", ">>");
            var prefixedSuffixedKeys = Arrays.stream(ArrayUtils.nullToEmpty(keys)).map(key -> "<<"+key+">>").toArray(String[]::new);
            return StringUtils.replaceEachRepeatedly(template, prefixedSuffixedKeys,
                    ArrayUtils.toStringArray(values, ""));
        }
    },

    /** A apache commons style ${somekey} pattern.*/
    $BRACES("\\$\\{(?=[a-zA-Z0-9]*[a-zA-Z])[a-zA-Z0-9]+\\}") {
        /**{@inheritDoc}. An apache commons style ${somekey} pattern for replacing values.*/
        @Override
        public String format(String template, Object... values) {
            // If the values is just a Map (of key,value) then replace/substitute each key with mapping value from map
            if (values.length == 1 && values[0] instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) values[0];
                return StringSubstitutor.replace(template, map, "${", "}");
            }

            //In case if the values are in a var-args style such as value-1, value-2,....
            final String[] keys = StringUtils.substringsBetween(template, "${", "}");
            var prefixedSuffixedKeys = Arrays.stream(ArrayUtils.nullToEmpty(keys)).map(key -> "${"+key+"}").toArray(String[]::new);
            return StringUtils.replaceEachRepeatedly(template, prefixedSuffixedKeys,
                    ArrayUtils.toStringArray(values, ""));
        }
    },

    /** A named {key} pattern which would be replaced by a mapped value from a key-value map.*/
    NAMEDARGS("\\s+\\{(?=[a-zA-Z0-9]*[a-zA-Z])[a-zA-Z0-9]+\\}") {
        /**
         * {@inheritDoc}.
         * values can either be "just a" key-value map; or could be a vararg style arguments
         */
        public String format(String template, Object... values) {
            //validate(template);
            // If the values is just a Map (of key,value) then replace/substitute each key with mapping value from map
            if (values.length == 1 && values[0] instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) values[0];
                return StringSubstitutor.replace(template, map, "{", "}");
            }

            //In case if the values are in a var-args style such as value-1, value-2,....
            final String[] keys = StringUtils.substringsBetween(template, "{", "}");
            var prefixedSuffixedKeys = Arrays.stream(ArrayUtils.nullToEmpty(keys)).map(key -> "{"+key+"}").toArray(String[]::new);
            return StringUtils.replaceEachRepeatedly(template, prefixedSuffixedKeys,
                    ArrayUtils.toStringArray(values, ""));
        }
    },

    /** A Standard Java style MessageFormat pattern - just the way printf works.*/
    JAVA("\\{\\d+\\}") {
        /**{@inheritDoc.}*/
        public String format(String template, Object... values) {
            //validate(template);
            return MessageFormat.format(template, values);
        }
    };

    /** A pattern that this instance represents.*/
    private final Pattern pattern;
    private final Predicate<String> matcher;
    private final Predicate<String> finder;

    /**
     * Constructor.
     * @param patternString a regex based string to validate th a string template
     */
    DetailsMessageFormatters(String patternString) {
        pattern = Pattern.compile(patternString);
        matcher = pattern.asMatchPredicate();
        finder = pattern.asPredicate();
    }

    /**
     * Method to format a given template and values.
     *
     * @param template a marker-ed template where each marker style is specific to the instance's {@link #pattern}
     * @param values could be var-arg style argument values that is allowed in all the formats. However it could just be
     *               a map (of key-value pairs) if {@link #NAMEDARGS} is used.
     * @return formatted string
     */
    public abstract String format(String template, Object... values);

    /** All valid enums set. exclude any such enum not to be used for searching pattern.*/
    private static final EnumSet<DetailsMessageFormatters> enumSet = EnumSet.complementOf(EnumSet.of(NONE));

    /**
     * A detection of formatter given a template
     * @param template to be introspected to know which formatter style works to replace values
     * @return the matching {@link DetailsMessageFormatters} iff only one formatter matches.
     * @throws IllegalArgumentException in case of no suitable formatter determined or multiple formatters matching.
     */
    public static DetailsMessageFormatters detectFormatter(String template) {
        var matchingFormatters = enumSet.stream().filter(en -> en.finder.test(template)).collect(Collectors.toSet());

        if (matchingFormatters.size() == 1) {
            //The template should match strictly to only one and only one pattern
            return matchingFormatters.iterator().next();
        }
        // else throw exception. If you really need to handle this exception; then wrap this method call with a Try
        throw new IllegalArgumentException(
                "Template '%s' keys are matching to none or multiple patterns (%s)"
                    .formatted(template, matchingFormatters));
    }

    /**
     * A convenient static method to determine format and format
     * @param template the string template
     * @param values the values to replace
     * @return formated template with values (if required with {@link #NONE}).
     */
    public static String detectAndFormat(String template, Object... values) {
        return Try.of(() -> detectFormatter(template)).getOrElse(NONE).format(template, values);
    }
}