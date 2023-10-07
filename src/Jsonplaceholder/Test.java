package Jsonplaceholder;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.Comparator;

import com.google.gson.Gson;


public class Test {
    static final HttpClient client = HttpClient.newHttpClient();
    public static final String URL_USERS = "https://jsonplaceholder.typicode.com/users";
    public static final String URL_POSTS = "https://jsonplaceholder.typicode.com/posts/";

    public static void main(String[] args) {

        User newuser = new User(0, "Serg", "Serg", "ty@gmail.com",
                new Address("street", "suite", "city", "zipcode", new Geo(1, 1)), "phone", "website");

        try {

            System.out.println("addUser(newuser) = " + addUser(newuser));
            User userToMod = new Gson().fromJson(getUserById(5), User.class);
            userToMod.setPhone("my phone");
            System.out.println("updateUser(newuser) = " + updateUser(userToMod));
            System.out.println("deleteUser(5) = " + deleteUser(5));
            System.out.println("getUsers() = " + getUsers());
            System.out.println("getUserById(5) = " + getUserById(5));
            System.out.println("getUserByName(\"Moriah.Stanton\") = " + getUserByName("Moriah.Stanton"));
            System.out.println("getUserTasks(1) = " + getUserTasks(1));
            SaveLastPostComments(10);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static String addUser(User user) throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL_USERS))
                .POST(HttpRequest.BodyPublishers.ofString(new Gson().toJson(user)))
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString()).body();
    }

    public static String updateUser(User user) throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL_USERS + "/" + user.getId()))
                .PUT(HttpRequest.BodyPublishers.ofString(new Gson().toJson(user)))
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString()).body();
    }

    public static String deleteUser(int userID) throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL_USERS + "/" + userID))
                .DELETE()
                .build();

        return String.valueOf(client.send(request, HttpResponse.BodyHandlers.ofString()).statusCode());
    }

    public static String getUsers() throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL_USERS))
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString()).body();
    }

    public static String getUserById(int userId) throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL_USERS + "/" + userId))
                .GET()
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString()).body();
    }

    public static String getUserByName(String name) throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL_USERS + "?username=" + name))
                .GET()
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString()).body();
    }

    public static String getUserTasks(int userId) throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL_USERS + "/" + userId + "/todos"))
                .GET()
                .build();

        String answer = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
        Task[] json = new Gson().fromJson(answer, Task[].class);
        return Arrays.toString(Arrays.stream(json).filter((x) -> (!x.isCompleted())).toArray());
    }

    public static void SaveLastPostComments(int userId) throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL_USERS + "/" + userId + "/posts"))
                .GET()
                .build();

        String answer = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
        Post[] json = new Gson().fromJson(answer, Post[].class);
        int maxPostId = Arrays.stream(json).max(Comparator.comparingInt(Post::getId)).get().getId();

        HttpRequest requestPost = HttpRequest.newBuilder()
                .uri(URI.create(URL_POSTS + maxPostId + "/comments"))
                .GET()
                .build();

        String FileName = String.format("user-%d-post-%d-comments.json", userId, maxPostId);

        try (PrintWriter out = new PrintWriter(new FileWriter(FileName))) {
            out.write(client.send(requestPost, HttpResponse.BodyHandlers.ofString()).body());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

