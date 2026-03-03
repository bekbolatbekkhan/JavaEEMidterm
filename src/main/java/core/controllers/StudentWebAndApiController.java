package core.controllers;

import core.entities.Student;
import core.entities.StudentProfile;
import core.repositories.CourseRepository;
import core.repositories.StudentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping
public class StudentWebAndApiController {

    private final StudentRepository studentRepo;
    private final CourseRepository courseRepo;

    public StudentWebAndApiController(StudentRepository studentRepo,
                                      CourseRepository courseRepo) {
        this.studentRepo = studentRepo;
        this.courseRepo = courseRepo;
    }

    // ==================================================
    // ====================== WEB =======================
    // ==================================================

    @GetMapping("/students")
    public String showAllStudents(Model model) {
        model.addAttribute("students", studentRepo.findAll());
        return "students";
    }

    @GetMapping("/students/new")
    public String showNewStudentForm(Model model) {
        model.addAttribute("student", new Student());
        model.addAttribute("profile", new StudentProfile());
        return "student-form";
    }

    @PostMapping("/students")
    public String createStudentFromForm(@ModelAttribute Student student,
                                        @RequestParam String address,
                                        @RequestParam String phone,
                                        @RequestParam String birthDate,
                                        Model model) {

        Optional<Student> existing = studentRepo.findByEmail(student.getEmail());

        if (existing.isPresent()) {
            model.addAttribute("error", "Email уже используется другим студентом");
            model.addAttribute("student", student);
            model.addAttribute("profile", new StudentProfile());
            return "student-form";
        }

        StudentProfile profile = new StudentProfile();
        profile.setAddress(address);
        profile.setPhone(phone);
        profile.setBirthDate(LocalDate.parse(birthDate));

        student.setProfile(profile);
        studentRepo.save(student);

        return "redirect:/students";
    }

    @DeleteMapping("/students/{id}")
    public String removeStudentFromWeb(@PathVariable Long id) {
        studentRepo.deleteById(id);
        return "redirect:/students";
    }

    // ==================================================
    // ======================= API ======================
    // ==================================================

    @ResponseBody
    @GetMapping("/api/students")
    public List<Student> getAllStudentsApi() {
        return studentRepo.findAll();
    }

    @ResponseBody
    @GetMapping("/api/students/{id}")
    public ResponseEntity<Student> getStudentByIdApi(@PathVariable Long id) {
        return studentRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @ResponseBody
    @PostMapping("/api/students")
    public Student addStudentApi(@RequestBody Student student) {
        return studentRepo.save(student);
    }

    @ResponseBody
    @PostMapping("/api/students/{sid}/courses/{cid}")
    public ResponseEntity<String> linkCourseApi(@PathVariable Long sid,
                                                @PathVariable Long cid) {

        Student student = studentRepo.findById(sid).orElse(null);
        if (student == null) {
            return ResponseEntity.notFound().build();
        }

        courseRepo.findById(cid).ifPresent(course -> {
            student.getCourses().add(course);
            studentRepo.save(student);
        });

        return ResponseEntity.ok("Курс назначен");
    }

    @ResponseBody
    @DeleteMapping("/api/students/{id}")
    public ResponseEntity<String> removeStudentApi(@PathVariable Long id) {

        if (!studentRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        studentRepo.deleteById(id);
        return ResponseEntity.ok("Студент удалён");
    }
}