package uts.dimas.nim230309006.service;

import uts.dimas.nim230309006.model.CourseGrade;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class GradeCalculatorTest {

    private final GradeCalculator calculator = new GradeCalculator();

    // =============== TEST calculateGPA ===============

    @Test
    public void testCalculateGPA_NullList_ReturnsZero() {
        assertEquals(0.0, calculator.calculateGPA(null));
    }

    @Test
    public void testCalculateGPA_EmptyList_ReturnsZero() {
        assertEquals(0.0, calculator.calculateGPA(new ArrayList<>()));
    }

    @Test
    public void testCalculateGPA_NormalCase_ReturnsCorrectGPA() {
        List<CourseGrade> grades = Arrays.asList(
                new CourseGrade("MK1", 3, 4.0), // A = 4.0, SKS = 3
                new CourseGrade("MK2", 2, 3.0)  // B = 3.0, SKS = 2
        );
        assertEquals(3.6, calculator.calculateGPA(grades));
    }

    @Test
    public void testCalculateGPA_TotalCreditsZero_ReturnsZero() {
        List<CourseGrade> grades = Arrays.asList(
                new CourseGrade("MK1", 0, 4.0),
                new CourseGrade("MK2", 0, 3.0)
        );
        assertEquals(0.0, calculator.calculateGPA(grades));
    }

    @Test
    public void testCalculateGPA_InvalidGradePointBelowZero_ThrowsException() {
        List<CourseGrade> grades = Arrays.asList(
                new CourseGrade("MK1", 3, -1.0) // gradePoint < 0
        );
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculateGPA(grades)
        );
        assertTrue(ex.getMessage().contains("Invalid grade point"));
    }

    @Test
    public void testCalculateGPA_InvalidGradePointAboveFour_ThrowsException() {
        List<CourseGrade> grades = Arrays.asList(
                new CourseGrade("MK1", 3, 5.0) // gradePoint > 4.0
        );
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculateGPA(grades)
        );
        assertTrue(ex.getMessage().contains("Invalid grade point"));
    }

    // =============== TEST determineAcademicStatus ===============

    @ParameterizedTest
    @MethodSource("provideAcademicStatusCases")
    public void testDetermineAcademicStatus_ValidInputs_ReturnsCorrectStatus(double gpa, int semester, String expected) {
        assertEquals(expected, calculator.determineAcademicStatus(gpa, semester));
    }

    private static Stream<Arguments> provideAcademicStatusCases() {
        return Stream.of(
                Arguments.of(2.0, 1, "ACTIVE"),
                Arguments.of(1.9, 2, "PROBATION"),
                Arguments.of(2.25, 3, "ACTIVE"),
                Arguments.of(2.24, 4, "PROBATION"),
                Arguments.of(1.99, 4, "SUSPENDED"),
                Arguments.of(2.5, 5, "ACTIVE"),
                Arguments.of(2.49, 6, "PROBATION"),
                Arguments.of(1.99, 7, "SUSPENDED")
        );
    }

    @ParameterizedTest
    @ValueSource(doubles = {-0.1, 4.1, 5.0})
    public void testDetermineAcademicStatus_InvalidGPA_ThrowsException(double gpa) {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> calculator.determineAcademicStatus(gpa, 1)
        );
        assertTrue(ex.getMessage().contains("GPA must be between 0 and 4.0"));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -5})
    public void testDetermineAcademicStatus_InvalidSemester_ThrowsException(int semester) {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> calculator.determineAcademicStatus(3.0, semester)
        );
        assertTrue(ex.getMessage().contains("Semester must be positive"));
    }

    // =============== TEST calculateMaxCredits ===============

    @ParameterizedTest
    @MethodSource("provideMaxCreditsCases")
    public void testCalculateMaxCredits_ValidGPA_ReturnsCorrectMaxCredits(double gpa, int expected) {
        assertEquals(expected, calculator.calculateMaxCredits(gpa));
    }

    private static Stream<Arguments> provideMaxCreditsCases() {
        return Stream.of(
                Arguments.of(4.0, 24),
                Arguments.of(3.0, 24),
                Arguments.of(2.99, 21),
                Arguments.of(2.5, 21),
                Arguments.of(2.49, 18),
                Arguments.of(2.0, 18),
                Arguments.of(1.99, 15),
                Arguments.of(0.0, 15)
        );
    }

    @ParameterizedTest
    @ValueSource(doubles = {-0.1, 4.1})
    public void testCalculateMaxCredits_InvalidGPA_ThrowsException(double gpa) {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculateMaxCredits(gpa)
        );
        assertTrue(ex.getMessage().contains("GPA must be between 0 and 4.0"));
    }
}