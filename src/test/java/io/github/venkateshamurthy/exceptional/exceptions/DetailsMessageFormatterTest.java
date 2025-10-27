package io.github.venkateshamurthy.exceptional.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Map;
import java.util.stream.Stream;

import static io.github.venkateshamurthy.exceptional.exceptions.DetailsMessageFormatters.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
class DetailsMessageFormatterTest {
    @ParameterizedTest @EnumSource(value = DetailsMessageFormatters.class)
    @DisplayName("Validating all the DetailsMessageFormatters enums")
    void testValidation(DetailsMessageFormatters en) {
        assertThatThrownBy(()->{
            //en.validate("{0}{JK}{}");
            detectFormatter("<<asdad>>${dollar}{0}{JK}{}");
        });
    }

    @ParameterizedTest @ArgumentsSource(Input.class)
    void test(Input input) {
        assertThat(input.en.format(input.template, input.args)).isEqualToIgnoringCase(Input.expected);
        assertThat(detectAndFormat(input.template, input.args)).isEqualToIgnoringCase(Input.expected);
    }

    private record Input (DetailsMessageFormatters en, String template, Object...args)  implements ArgumentsProvider{
        private Input(){this(SLF4J, "");}
        static final String expected = "Given my understanding came to be false, My experiments is always with the truth";
        private static Stream<Input> getInputs() {
            return Stream.of(
                    new Input(DOUBLEANGULAR, "Given my <<experiences>> came to be <<real>>, My <<exploration>> is always with the <<reality>>", Map.of("experiences","understanding", "real","false", "exploration", "experiments", "reality","truth")),
                    new Input(DOUBLEANGULAR, "Given my <<experiences>> came to be <<real>>, My <<exploration>> is always with the <<reality>>", "understanding", "false","experiments", "truth"),
                    new Input(DOUBLEANGULAR, "Given my <<experiences>> came to be <<real>>, My <<exploration>> is always with the <<reality>>", "understanding", "false","experiments", "truth"),
                    new Input(DOUBLEANGULAR, "Given my <<experiences>> came to be false, My experiments is always with the truth", "understanding"),

                    new Input($BRACES, "Given my understanding came to be false, My experiments is always with the truth"),
                    new Input($BRACES, "Given my ${experiences} came to be ${real}, My ${exploration} is always with the ${reality}", Map.of("experiences","understanding", "real","false", "exploration", "experiments", "reality","truth")),
                    new Input($BRACES, "Given my ${experiences } came to be ${ real}, My ${ exploration } is always with the ${reality}", "understanding", "false","experiments", "truth"),
                    new Input($BRACES, "Given my ${experiences} came to be false, My experiments is always with the truth", "understanding"),

                    new Input(JAVA, "Given my {0} came to be {1}, My {2} is always with the {3}", "understanding", "false","experiments", "truth"),
                    new Input(JAVA, "Given my {0} came to be {1}, My {2} is always with the {3}", "understanding", "false","experiments", "truth"),
                    new Input(SLF4J, "Given my {} came to be {}, My {} is always with the {}", "understanding", "false","experiments", "truth"),
                    new Input(NAMEDARGS, "Given my understanding came to be false, My experiments is always with the truth"),
                    new Input(NAMEDARGS, "Given my {experiences} came to be {real}, My {exploration} is always with the {reality}", Map.of("experiences","understanding", "real","false", "exploration", "experiments", "reality","truth")),
                    new Input(NAMEDARGS, "Given my {experiences } came to be { real}, My { exploration } is always with the {reality}", "understanding", "false","experiments", "truth"),
                    new Input(NAMEDARGS, "Given my {experiences} came to be false, My experiments is always with the truth", "understanding")
            );
        }

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
            return getInputs().map(Arguments::of);
        }
    }
}
