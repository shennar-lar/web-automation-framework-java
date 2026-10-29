package apiTests;

import org.testng.annotations.Test;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import static org.hamcrest.Matchers.*;
import static io.restassured.RestAssured.given;
import java.util.List;
import java.util.Map;

public class UnlikeMultipleSongsTest {

    @Test
    public void verifyUserCanUnlikeMultipleSongs() {

        String endpoint =
                "https://qa.koel.app/api/interaction/batch/unlike";

        String firstSongId =
                "0b794968a26cd03bb533762affc8c0ca";

        String secondSongId =
                "0c3d784a530e74f9e5dbcad9c2711cd2";

        Map<String, Object> requestBody = Map.of(
                "songs", List.of(firstSongId, secondSongId)
        );
        String token = System.getenv("KOEL_TOKEN");
        Response response =
                given()
                        .contentType(ContentType.JSON)
                        .auth()
                        .oauth2(token)
                        .body(requestBody)
                        .when()
                        .post(endpoint);

        response.prettyPrint();
        response.then()
                .statusCode(200)
                .body("size()", equalTo(2))
                .body("song_id", containsInAnyOrder(firstSongId, secondSongId))
                .body("liked", everyItem(equalTo(false)))
                .body("song", everyItem(notNullValue()))
                .body("user", everyItem(notNullValue()));
    }
}