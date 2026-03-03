package core.controllers;

import core.entities.Course;
import core.entities.Lesson;
import core.entities.Student;
import core.repositories.CourseRepository;
import core.repositories.StudentRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping
public class HomeController {

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    public HomeController(StudentRepository studentRepository,
                          CourseRepository courseRepository) {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
    }

    // ==================================================
    // ===================== HOME =======================
    // ==================================================

    @GetMapping("/")
    public String showHomePage() {
        return "index";
    }

    // ==================================================
    // ================= STUDENT-COURSE =================
    // ==================================================

    @GetMapping("/assign")
    public String showAssignForm(Model model) {
        model.addAttribute("students", studentRepository.findAll());
        model.addAttribute("courses", courseRepository.findAll());
        return "assign";
    }

    @PostMapping("/assign")
    public String processAssign(@RequestParam Long studentId,
                                @RequestParam Long courseId) {

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Студент не найден"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Курс не найден"));

        if (!student.getCourses().contains(course)) {
            student.getCourses().add(course);
            studentRepository.save(student);
        }

        return "redirect:/students";
    }

    // ==================================================
    // ===================== LESSONS ====================
    // ==================================================

    @GetMapping("/courses/{courseId}/lessons/new")
    public String showNewLessonForm(@PathVariable Long courseId,
                                    Model model) {

        courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Курс не найден"));

        model.addAttribute("lesson", new Lesson());
        model.addAttribute("courseId", courseId);

        return "lesson-form";
    }

    @PostMapping("/courses/{courseId}/lessons")
    public String saveNewLesson(@PathVariable Long courseId,
                                @ModelAttribute Lesson lesson) {

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Курс не найден"));

        lesson.setCourse(course);
        course.getLessons().add(lesson);

        courseRepository.save(course);

        return "redirect:/courses";
    }
}