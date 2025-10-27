/*
 * Copyright (c) 2025, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.identifier;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test cases for the identifier encoding/decoding utility methods.
 *
 * @since 2.0.0
 */
public class UtilsTest {

    @DataProvider(name = "escapeSpecialCharactersData")
    public Object[][] escapeSpecialCharactersData() {
        return new Object[][]{
                {"normal", "normal"},
                {"test$name", "test\\$name"},
                {"test&name", "test\\&name"},
                {"test+name", "test\\+name"},
                {"test,name", "test\\,name"},
                {"test:name", "test\\:name"},
                {"test;name", "test\\;name"},
                {"test=name", "test\\=name"},
                {"test?name", "test\\?name"},
                {"test@name", "test\\@name"},
                {"test#name", "test\\#name"},
                {"test\\name", "test\\\\name"},
                {"test|name", "test\\|name"},
                {"test/name", "test\\/name"},
                {"test'name", "test\\'name"},
                {"test name", "test\\ name"},
                {"test[name", "test\\[name"},
                {"test}name", "test\\}name"},
                {"test]name", "test\\]name"},
                {"test<name", "test\\<name"},
                {"test>name", "test\\>name"},
                {"test.name", "test\\.name"},
                {"test\"name", "test\\\"name"},
                {"test^name", "test\\^name"},
                {"test*name", "test\\*name"},
                {"test{name", "test\\{name"},
                {"test~name", "test\\~name"},
                {"test`name", "test\\`name"},
                {"test(name", "test\\(name"},
                {"test)name", "test\\)name"},
                {"test%name", "test\\%name"},
                {"test!name", "test\\!name"},
                {"test-name", "test\\-name"},
                {"$&+,:;=?@#\\|/' []<>.\"^*{}~`()%!-", "\\$\\&\\+\\,\\:\\;\\=\\?\\@\\#\\\\\\|\\/\\'\\ \\[\\]\\<\\>\\.\\\"\\^\\*\\{\\}\\~\\`\\(\\)\\%\\!\\-"},
                {"", ""},
                {"noSpecialChars", "noSpecialChars"}
        };
    }

    @Test(dataProvider = "escapeSpecialCharactersData")
    public void testEscapeSpecialCharacters(String input, String expected) {
        String result = Utils.escapeSpecialCharacters(input);
        Assert.assertEquals(result, expected);
    }

    @DataProvider(name = "unescapeJavaData")
    public Object[][] unescapeJavaData() {
        return new Object[][]{
                {"normal", "normal"},
                {"test\\nname", "test\nname"},
                {"test\\tname", "test\tname"},
                {"test\\rname", "test\rname"},
                {"test\\\\name", "test\\name"},
                {"test\\'name", "test'name"},
                {"test\\\"name", "test\"name"},
                {"\\u0041", "A"},
                {"\\u0061", "a"},
                {"", ""},
                {null, null}
        };
    }

    @Test(dataProvider = "unescapeJavaData")
    public void testUnescapeJava(String input, String expected) {
        String result = Utils.unescapeJava(input);
        Assert.assertEquals(result, expected);
    }

    @DataProvider(name = "decodeIdentifierData")
    public Object[][] decodeIdentifierData() {
        return new Object[][]{
                // Basic decoding
                {"normal", "normal"},
                {"&0036", "$"},
                {"&0092", "\\"},
                {"&0046", "."},
                {"&0058", ":"},
                {"&0059", ";"},
                {"&0091", "["},
                {"&0093", "]"},
                {"&0047", "/"},
                {"&0060", "<"},
                {"&0062", ">"},
                
                // Multiple encodings
                {"test&0046name", "test.name"},
                {"&0060init&0062", "<init>"},
                {"&0046&0060init&0062", ".<init>"},
                
                // Generated method names
                {"$gen$test", "test"},
                {"$gen$&0046&0060init&0062", ".<init>"},
                {"$gen$&0046&0060start&0062", ".<start>"},
                {"$gen$&0046&0060stop&0062", ".<stop>"},
                {"$gen$&0046&0060testinit&0062", ".<testinit>"},
                
                // Non-generated method names (should remain unchanged)
                {"regularMethod", "regularMethod"},
                
                // Invalid unicode points (should keep &)
                {"&abcd", "&abcd"},
                {"&12ab", "&12ab"},
                {"&", "&"},
                {"&123", "&123"},
                {"&12345", "Ӓ5"}, // 5 digits, but first 4 (1234) are valid unicode point
                
                // Edge cases
                {null, null},
                {"", ""},
                
                // Mixed content
                {"test&0046name&0058value", "test.name:value"},
                {"prefix&0091index&0093", "prefix[index]"}
        };
    }

    @Test(dataProvider = "decodeIdentifierData")
    public void testDecodeIdentifier(String input, String expected) {
        String result = Utils.decodeIdentifier(input);
        Assert.assertEquals(result, expected);
    }

    @DataProvider(name = "unescapeBallerinaData")
    public Object[][] unescapeBallerinaData() {
        return new Object[][]{
                {"normal", "normal"},
                {"test\\nname", "test\nname"},
                {"test\\u{0041}", "testA"},
                {"test\\u{61}", "testa"},
                {"test\\u{1F600}", "test😀"}, // Emoji (code point > 0xFFFF)
                {"\\u{0048}ello", "Hello"},
                {"test\\\\u{61}", "test\\u{61}"}, // Escaped numeric escape
                {"test\\\\\\u{61}", "test\\a"}, // One backslash + unicode
                {"\\\\u{61}", "\\u{61}"}, // Escaped at start
                {"test\\u{5C}", "test\\"}, // Unicode for backslash
                {"", ""}
        };
    }

    @Test(dataProvider = "unescapeBallerinaData")
    public void testUnescapeBallerina(String input, String expected) {
        String result = Utils.unescapeBallerina(input);
        Assert.assertEquals(result, expected);
    }

    @DataProvider(name = "unescapeUnicodeCodepointsData")
    public Object[][] unescapeUnicodeCodepointsData() {
        return new Object[][]{
                {"normal", "normal"},
                {"\\u{0041}", "A"},
                {"\\u{61}", "a"},
                {"\\u{0048}ello", "Hello"},
                {"test\\u{1F600}emoji", "test😀emoji"},
                {"\\u{1F44D}", "👍"},
                
                // Escaped numeric escapes (should not be replaced)
                {"\\\\u{61}", "\\\\u{61}"},
                {"\\\\\\u{61}", "\\\\a"},
                {"\\\\\\\\u{61}", "\\\\\\\\u{61}"},
                
                // Special case: backslash unicode
                {"\\u{5C}", "\\u005C"}, // Backslash gets special treatment
                
                // Multiple unicode escapes
                {"\\u{48}\\u{65}\\u{6C}\\u{6C}\\u{6F}", "Hello"},
                
                // Edge cases
                {"", ""},
                {"no unicode here", "no unicode here"},
                
                // Mixed content
                {"prefix\\u{41}middle\\u{42}suffix", "prefixAmiddleBsuffix"}
        };
    }

    @Test(dataProvider = "unescapeUnicodeCodepointsData")
    public void testUnescapeUnicodeCodepoints(String input, String expected) {
        String result = Utils.unescapeUnicodeCodepoints(input);
        Assert.assertEquals(result, expected);
    }

    @DataProvider(name = "isEscapedNumericEscapeData")
    public Object[][] isEscapedNumericEscapeData() {
        return new Object[][]{
                {"", true},      // 0 backslashes (even) - returns false (not escaped)
                {"\\", false},    // 1 backslash (odd) - returns true (escaped)
                {"\\\\", true},   // 2 backslashes (even) - returns false (not escaped)
                {"\\\\\\", false}, // 3 backslashes (odd) - returns true (escaped)
                {"\\\\\\\\", true}, // 4 backslashes (even) - returns false (not escaped)
        };
    }

    @Test(dataProvider = "isEscapedNumericEscapeData")
    public void testIsEscapedNumericEscape(String leadingSlashes, boolean expectedNotEscaped) {
        // The method returns true if escaped, false if not escaped
        boolean result = Utils.isEscapedNumericEscape(leadingSlashes);
        Assert.assertEquals(result, !expectedNotEscaped);
    }

    @DataProvider(name = "encodeFunctionIdentifierData")
    public Object[][] encodeFunctionIdentifierData() {
        return new Object[][]{
                // Special function names
                {".<init>", "$gen$&0046&0060init&0062"},
                {".<start>", "$gen$&0046&0060start&0062"},
                {".<stop>", "$gen$&0046&0060stop&0062"},
                {".<testinit>", "$gen$&0046&0060testinit&0062"},
                
                // Normal function names
                {"normalFunction", "normalFunction"},
                {"myFunction", "myFunction"},
                
                // Function names with JVM reserved characters
                {"test.method", "$gen$test&0046method"},
                {"test:method", "$gen$test&0058method"},
                {"test;method", "$gen$test&0059method"},
                {"test[method", "$gen$test&0091method"},
                {"test]method", "$gen$test&0093method"},
                {"test/method", "$gen$test&0047method"},
                {"test<method", "$gen$test&0060method"},
                {"test>method", "$gen$test&0062method"},
                {"test\\method", "testmethod"}, // Backslash gets unescaped in encodeIdentifier
                
                // Function names with escaped special characters
                {"test\\$method", "test&0036method"}, // $ gets encoded but no $gen$ prefix since no JVM reserved chars after encoding
                
                // Mixed content
                {"test.method:value", "$gen$test&0046method&0058value"},
                
                // Edge cases
                {"", ""},
                
                // Names without JVM reserved chars should not be prefixed
                {"simpleMethod", "simpleMethod"},
                {"method_name", "method_name"},
                {"method123", "method123"}
        };
    }

    @Test(dataProvider = "encodeFunctionIdentifierData")
    public void testEncodeFunctionIdentifier(String input, String expected) {
        String result = Utils.encodeFunctionIdentifier(input);
        Assert.assertEquals(result, expected);
    }

    @DataProvider(name = "encodeNonFunctionIdentifierData")
    public Object[][] encodeNonFunctionIdentifierData() {
        return new Object[][]{
                // Normal identifiers
                {"normal", "normal"},
                {"myVariable", "myVariable"},
                
                // Identifiers with JVM reserved characters
                {"test.field", "test&0046field"},
                {"test:field", "test&0058field"},
                {"test;field", "test&0059field"},
                {"test[field", "test&0091field"},
                {"test]field", "test&0093field"},
                {"test/field", "test&0047field"},
                {"test<field", "test&0060field"},
                {"test>field", "test&0062field"},
                {"test\\field", "test\field"}, // Backslash followed by f becomes \f (form feed) when unescaped
                
                // Identifiers with escaped special characters
                {"test\\$field", "test&0036field"},
                
                // Multiple reserved chars
                {"test.field:value", "test&0046field&0058value"},
                
                // Edge cases
                {"", ""},
                
                // Names without reserved chars
                {"simpleField", "simpleField"},
                {"field_name", "field_name"},
                {"field123", "field123"}
        };
    }

    @Test(dataProvider = "encodeNonFunctionIdentifierData")
    public void testEncodeNonFunctionIdentifier(String input, String expected) {
        String result = Utils.encodeNonFunctionIdentifier(input);
        Assert.assertEquals(result, expected);
    }

    @Test
    public void testRoundTripEncodeDecode() {
        // Test that encoding and then decoding gives back the original (or expected) value
        String[] testCases = {
                ".<init>",
                ".<start>",
                ".<stop>",
                ".<testinit>",
                "test.method",
                "test:field",
                "simple"
        };

        for (String testCase : testCases) {
            String encoded = Utils.encodeFunctionIdentifier(testCase);
            String decoded = Utils.decodeIdentifier(encoded);
            Assert.assertEquals(decoded, testCase, 
                    String.format("Round trip failed for '%s': encoded='%s', decoded='%s'", 
                            testCase, encoded, decoded));
        }
    }

    @Test
    public void testRoundTripNonFunctionEncodeDecode() {
        String[] testCases = {
                "test.field",
                "test:value",
                "simple",
                "field[index]"
        };

        for (String testCase : testCases) {
            String encoded = Utils.encodeNonFunctionIdentifier(testCase);
            String decoded = Utils.decodeIdentifier(encoded);
            Assert.assertEquals(decoded, testCase,
                    String.format("Round trip failed for '%s': encoded='%s', decoded='%s'",
                            testCase, encoded, decoded));
        }
    }

    @Test
    public void testComplexUnicodeEscapeSequences() {
        // Test complex scenarios with multiple unicode escapes
        String input = "\\u{48}\\u{65}\\u{6C}\\u{6C}\\u{6F} \\u{1F600}";
        String expected = "Hello 😀";
        String result = Utils.unescapeBallerina(input);
        Assert.assertEquals(result, expected);
    }

    @Test
    public void testMixedEscapeSequences() {
        // Test mixing unicode and regular Java escapes
        String input = "Line1\\nLine2\\u{0009}Tab";
        String expected = "Line1\nLine2\tTab";
        String result = Utils.unescapeBallerina(input);
        Assert.assertEquals(result, expected);
    }

    @Test
    public void testEscapedBackslashBeforeUnicode() {
        // Test that \\u{61} doesn't get unescaped
        String input = "test\\\\u{61}end";
        String result = Utils.unescapeUnicodeCodepoints(input);
        Assert.assertEquals(result, "test\\\\u{61}end");
    }

    @Test
    public void testMultipleBackslashesBeforeUnicode() {
        // Test various backslash combinations
        String input1 = "\\u{41}";         // 0 leading (should unescape)
        String input2 = "\\\\u{41}";       // 1 leading (should NOT unescape)
        String input3 = "\\\\\\u{41}";     // 2 leading (should unescape)
        String input4 = "\\\\\\\\u{41}";   // 3 leading (should NOT unescape)

        Assert.assertEquals(Utils.unescapeUnicodeCodepoints(input1), "A");
        Assert.assertEquals(Utils.unescapeUnicodeCodepoints(input2), "\\\\u{41}");
        Assert.assertEquals(Utils.unescapeUnicodeCodepoints(input3), "\\\\A");
        Assert.assertEquals(Utils.unescapeUnicodeCodepoints(input4), "\\\\\\\\u{41}");
    }

    @Test
    public void testSpecialCharacterEdgeCases() {
        // Test all special characters in one string
        String input = "$&+,:;=?@#\\|/' []<>.\"^*{}~`()%!-";
        String escaped = Utils.escapeSpecialCharacters(input);
        
        // Each character should be escaped
        Assert.assertTrue(escaped.contains("\\$"));
        Assert.assertTrue(escaped.contains("\\&"));
        Assert.assertTrue(escaped.contains("\\+"));
        Assert.assertTrue(escaped.contains("\\,"));
        Assert.assertTrue(escaped.contains("\\:"));
        Assert.assertTrue(escaped.contains("\\;"));
    }

    @Test
    public void testDecodeWithPartialUnicodePoint() {
        // Test strings that look like unicode points but aren't complete
        Assert.assertEquals(Utils.decodeIdentifier("&123"), "&123");   // Only 3 digits
        Assert.assertEquals(Utils.decodeIdentifier("&abc"), "&abc");   // Not all digits
        Assert.assertEquals(Utils.decodeIdentifier("&12a4"), "&12a4"); // Contains letter
        Assert.assertEquals(Utils.decodeIdentifier("test&0"), "test&0"); // Too short
    }

    @Test
    public void testEncodeSpecialCharactersInQuotedIdentifier() {
        // Test encoding of $ in quoted identifiers (escaped as \$)
        String input = "test\\$name";
        String encoded = Utils.encodeFunctionIdentifier(input);
        Assert.assertTrue(encoded.contains("&0036"));
    }

    @Test
    public void testEmptyAndNullInputs() {
        // Test empty strings
        Assert.assertEquals(Utils.escapeSpecialCharacters(""), "");
        Assert.assertEquals(Utils.encodeFunctionIdentifier(""), "");
        Assert.assertEquals(Utils.encodeNonFunctionIdentifier(""), "");
        Assert.assertEquals(Utils.decodeIdentifier(""), "");
        Assert.assertEquals(Utils.unescapeBallerina(""), "");
        Assert.assertEquals(Utils.unescapeUnicodeCodepoints(""), "");
        
        // Test null inputs
        Assert.assertNull(Utils.unescapeJava(null));
        Assert.assertNull(Utils.decodeIdentifier(null));
    }

    @Test
    public void testHighCodePointUnicodeCharacters() {
        // Test characters beyond the Basic Multilingual Plane (BMP)
        // Emoji and other characters with code points > 0xFFFF
        String input = "\\u{1F600}\\u{1F44D}\\u{1F389}"; // 😀👍🎉
        String result = Utils.unescapeBallerina(input);
        Assert.assertEquals(result, "😀👍🎉");
    }

    @Test
    public void testConsecutiveEncodedCharacters() {
        // Test multiple encoded characters in a row
        String encoded = "&0046&0058&0059"; // .:;
        String decoded = Utils.decodeIdentifier(encoded);
        Assert.assertEquals(decoded, ".:;");
    }

    @Test
    public void testUnicodeBackslashSpecialHandling() {
        // Test that \\u{5C} (backslash) is handled specially
        String input = "test\\u{5C}end";
        String result = Utils.unescapeUnicodeCodepoints(input);
        // Should be converted to \\u005C to preserve for Java unescaping
        Assert.assertEquals(result, "test\\u005Cend");
        
        // After Java unescaping
        String finalResult = Utils.unescapeJava(result);
        Assert.assertEquals(finalResult, "test\\end");
    }

    @Test
    public void testEncodingPreservesNonReservedCharacters() {
        // Test that characters not in the JVM reserved set are preserved
        String input = "test_method$123";
        String encoded = Utils.encodeFunctionIdentifier(input);
        
        // $ is in quoted identifier set, so if escaped it should be encoded
        // But _ and digits should be preserved
        Assert.assertTrue(encoded.contains("_"));
        Assert.assertTrue(encoded.contains("123"));
    }
}
