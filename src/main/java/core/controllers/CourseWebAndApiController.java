package core.controllers;

import core.entities.Course;
import core.entities.Lesson;
import core.repositories.CourseRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping
public class CourseWebAndApiController {

    private final CourseRepository courseRepo;

    public CourseWebAndApiController(CourseRepository courseRepo) {
        this.courseRepo = courseRepo;
    }

    // =========================
    // ----------- WEB ---------
    // =========================

    @GetMapping("/courses")
    public String showCoursesList(Model model) {
        model.addAttribute("courses", courseRepo.findAll());
        return "courses";
    }

    @GetMapping("/courses/new")
    public String showNewCourseForm(Model model) {
        model.addAttribute("course", new Course());
        return "course-form";
    }

    @PostMapping("/courses")
    public String createCourse(@ModelAttribute Course course) {
        courseRepo.save(course);
        return "redirect:/courses";
    }

    @GetMapping("/courses/{id}/lessons/new")
    public String showNewLessonForm(@PathVariable Long id, Model model) {
        model.addAttribute("lesson", new Lesson());
        model.addAttribute("courseId", id);
        return "lesson-form";
    }

    @PostMapping("/courses/{id}/lessons")
    public String addLessonToCourse(@PathVariable Long id,
                                    @ModelAttribute Lesson lesson) {

        courseRepo.findById(id).ifPresent(course -> {
            lesson.setCourse(course);
            course.getLessons().add(lesson);
            courseRepo.save(course);
        });

        return "redirect:/courses";
    }

    // =========================
    // ----------- API ---------
    // =========================

    @ResponseBody
    @GetMapping("/api/courses")
    public List<Course> listAll() {
        return courseRepo.findAll();
    }

    @ResponseBody
    @GetMapping("/api/courses/{id}")
    public ResponseEntity<Course> getOne(@PathVariable Long id) {
        return courseRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @ResponseBody
    @PostMapping("/api/courses")
    public Course create(@RequestBody Course course) {
        return courseRepo.save(course);
    }

    @ResponseBody
    @PostMapping("/api/courses/{courseId}/lessons")
    public ResponseEntity<String> addLesson(@PathVariable Long courseId,
                                            @RequestBody Lesson lesson) {

        return courseRepo.findById(courseId)
                .map(course -> {
                    lesson.setCourse(course);
                    course.getLessons().add(lesson);
                    courseRepo.save(course);
                    return ResponseEntity.ok("Урок добавлен");
                })
                .orElse(ResponseEntity.notFound().build());
    }
}