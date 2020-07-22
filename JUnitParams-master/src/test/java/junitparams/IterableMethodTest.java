package junitparams;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class IterableMethodTest {

    @Test
    @Parameters
    public void shouldHandleIterables(String a) {
        assertThat(a).isEqualTo("a");
    }

    public List<Object[]> parametersForShouldHandleIterables() {
        ArrayList<Object[]> params = new ArrayList<Object[]>();
        params.add(new Object[]{"a"});
        return params;
    }

    @Test
    @Parameters
    public void shouldHandleSimplifiedIterables(String a) {
        assertThat(a).isEqualTo("a");
    }

    public List<String> parametersForShouldHandleSimplifiedIterables() {
        return Arrays.asList("a");
    }

    @Test
    @Parameters
    public void shouldHandleIterableOfIterables(String a) {
        assertThat(a).isEqualTo("a");
    }

    public List<List<Object>> parametersForShouldHandleIterableOfIterables() {
        List<List<Object>> params = new ArrayList<List<Object>>();
        List<Object> nestedParams = new ArrayList<Object>();

        nestedParams.add("a");
        params.add(nestedParams);

        return params;
    }

    @Test
    @Parameters
    public void shouldHandleIterableParameters(Iterable<String> a) {
        assertThat(a)
                .hasSize(1)
                .containsOnly("a");
    }

    public List<List<String>> parametersForShouldHandleIterableParameters() {
        List<List<String>> params = new ArrayList<List<String>>();
        List<String> nestedParams = new ArrayList<String>();

        nestedParams.add("a");
        params.add(nestedParams);

        return params;
    }

    @Test
    @Parameters
    public void shouldHandleIterableParametersArr(Iterable<String> a) {
        assertThat(a)
                .hasSize(1)
                .containsOnly("a");
    }

    public Object[] parametersForShouldHandleIterableParametersArr() {
        List<List<String>> params = new ArrayList<List<String>>();
        List<String> nestedParams = new ArrayList<String>();

        nestedParams.add("a");
        params.add(nestedParams);

        return params.toArray();
    }
}
