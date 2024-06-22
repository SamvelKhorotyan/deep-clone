package deepclone;

import deepclone.domain.Man;
import deepclone.util.DeepCopyUtils;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<String> books = new ArrayList<>();
        books.add("Book1");
        books.add("Book2");

        Man originalMan = new Man("John", 25, books);
        Man copiedMan = DeepCopyUtils.deepCopy(originalMan);

        originalMan.setName("Doe");
        originalMan.setAge(30);
        originalMan.getFavoriteBooks().add("Book3");

        System.out.println("Original Man: " + originalMan.getName() + ", " + originalMan.getAge() + ", " + originalMan.getFavoriteBooks());
        System.out.println("Copied Man: " + copiedMan.getName() + ", " + copiedMan.getAge() + ", " + copiedMan.getFavoriteBooks());
    }
}
