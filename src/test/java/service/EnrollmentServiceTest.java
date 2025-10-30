package uts.dimas.nim230309006.service;

import uts.dimas.nim230309006.exception.*;
import uts.dimas.nim230309006.model.Course;
import uts.dimas.nim230309006.model.Enrollment;
import uts.dimas.nim230309006.model.Student;
import uts.dimas.nim230309006.repository.CourseRepository;
import uts.dimas.nim230309006.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EnrollmentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private GradeCalculator gradeCalculator;

    @InjectMocks
    private EnrollmentService enrollmentService;

    private Student student;
    private Course course;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup student
        student = new Student();
        student.setStudentId("S123");
        student.setEmail("dimas@example.com");
        student.setGpa(3.0);
        student.setAcademicStatus("ACTIVE");

        // Setup course
        course = new Course();
        course.setCourseCode("MK101");
        course.setCourseName("Pemrograman");
        course.setCapacity(30);
        course.setEnrolledCount(29); // masih bisa daftar
    }

    // =============== TEST enrollCourse ===============

    @Test
    public void testEnrollCourse_Success() {
        // Arrange
        when(studentRepository.findById("S123")).thenReturn(student);
        when(courseRepository.findByCourseCode("MK101")).thenReturn(course);
        when(courseRepository.isPrerequisiteMet("S123", "MK101")).thenReturn(true);

        // Act
        Enrollment result = enrollmentService.enrollCourse("S123", "MK101");

        // Assert
        assertNotNull(result);
        assertEquals("S123", result.getStudentId());
        assertEquals("MK101", result.getCourseCode());
        assertEquals("APPROVED", result.getStatus());

        // Verify interactions
        verify(courseRepository).update(course);
        assertEquals(30, course.getEnrolledCount());
        verify(notificationService).sendEmail(
                "dimas@example.com",
                "Enrollment Confirmation",
                "You have been enrolled in: Pemrograman");
    }

    @Test
    public void testEnrollCourse_StudentNotFound_ThrowsException() {
        when(studentRepository.findById("S999")).thenReturn(null);

        StudentNotFoundException ex = assertThrows(
                StudentNotFoundException.class,
                () -> enrollmentService.enrollCourse("S999", "MK101"));
        assertEquals("Student not found: S999", ex.getMessage());
    }

    @Test
    public void testEnrollCourse_StudentSuspended_ThrowsException() {
        student.setAcademicStatus("SUSPENDED");
        when(studentRepository.findById("S123")).thenReturn(student);

        EnrollmentException ex = assertThrows(
                EnrollmentException.class,
                () -> enrollmentService.enrollCourse("S123", "MK101"));
        assertEquals("Student is suspended", ex.getMessage());
    }

    @Test
    public void testEnrollCourse_CourseNotFound_ThrowsException() {
        when(studentRepository.findById("S123")).thenReturn(student);
        when(courseRepository.findByCourseCode("MK999")).thenReturn(null);

        CourseNotFoundException ex = assertThrows(
                CourseNotFoundException.class,
                () -> enrollmentService.enrollCourse("S123", "MK999"));
        assertEquals("Course not found: MK999", ex.getMessage());
    }

    @Test
    public void testEnrollCourse_CourseFull_ThrowsException() {
        course.setEnrolledCount(30); // sudah penuh
        when(studentRepository.findById("S123")).thenReturn(student);
        when(courseRepository.findByCourseCode("MK101")).thenReturn(course);

        CourseFullException ex = assertThrows(
                CourseFullException.class,
                () -> enrollmentService.enrollCourse("S123", "MK101"));
        assertEquals("Course is full", ex.getMessage());
    }

    @Test
    public void testEnrollCourse_PrerequisiteNotMet_ThrowsException() {
        when(studentRepository.findById("S123")).thenReturn(student);
        when(courseRepository.findByCourseCode("MK101")).thenReturn(course);
        when(courseRepository.isPrerequisiteMet("S123", "MK101")).thenReturn(false);

        PrerequisiteNotMetException ex = assertThrows(
                PrerequisiteNotMetException.class,
                () -> enrollmentService.enrollCourse("S123", "MK101"));
        assertEquals("Prerequisites not met", ex.getMessage());
    }

    // =============== TEST validateCreditLimit ===============

    @Test
    public void testValidateCreditLimit_ValidCredits_ReturnsTrue() {
        when(studentRepository.findById("S123")).thenReturn(student);
        when(gradeCalculator.calculateMaxCredits(3.0)).thenReturn(24);

        boolean result = enrollmentService.validateCreditLimit("S123", 20);
        assertTrue(result);
    }

    @Test
    public void testValidateCreditLimit_ExceedsLimit_ReturnsFalse() {
        when(studentRepository.findById("S123")).thenReturn(student);
        when(gradeCalculator.calculateMaxCredits(2.0)).thenReturn(18);

        boolean result = enrollmentService.validateCreditLimit("S123", 20);
        assertFalse(result);
    }

    @Test
    public void testValidateCreditLimit_StudentNotFound_ThrowsException() {
        when(studentRepository.findById("S999")).thenReturn(null);

        StudentNotFoundException ex = assertThrows(
                StudentNotFoundException.class,
                () -> enrollmentService.validateCreditLimit("S999", 10));
        assertEquals("Student not found", ex.getMessage());
    }

    // =============== TEST dropCourse ===============

    @Test
    public void testDropCourse_Success() {
        when(studentRepository.findById("S123")).thenReturn(student);
        when(courseRepository.findByCourseCode("MK101")).thenReturn(course);

        enrollmentService.dropCourse("S123", "MK101");

        verify(courseRepository).update(course);
        assertEquals(28, course.getEnrolledCount());
        verify(notificationService).sendEmail(
                "dimas@example.com",
                "Course Drop Confirmation",
                "You have dropped: Pemrograman");
    }

    @Test
    public void testDropCourse_StudentNotFound_ThrowsException() {
        when(studentRepository.findById("S999")).thenReturn(null);

        StudentNotFoundException ex = assertThrows(
                StudentNotFoundException.class,
                () -> enrollmentService.dropCourse("S999", "MK101"));
        assertEquals("Student not found", ex.getMessage());
    }

    @Test
    public void testDropCourse_CourseNotFound_ThrowsException() {
        when(studentRepository.findById("S123")).thenReturn(student);
        when(courseRepository.findByCourseCode("MK999")).thenReturn(null);

        CourseNotFoundException ex = assertThrows(
                CourseNotFoundException.class,
                () -> enrollmentService.dropCourse("S123", "MK999"));
        assertEquals("Course not found", ex.getMessage());
    }
}