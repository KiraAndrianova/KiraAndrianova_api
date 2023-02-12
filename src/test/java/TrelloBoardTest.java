import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TrelloBoardTest {

    private static RequestSpecification reqSpec;
    private static ResponseSpecification respSpec;
    private static String boardId;
    private static String listId;

    static final String urlWithBoardId = "/1/boards/{id}";
    static final String urlForBoards = "/1/boards";
    static final String urlForCards = "/1/cards";
    static final String urlForLists = "/1/lists";

    static final String boardName = "boardFromIdea";
    static final String newBoardName = "newName";
    static final String listName = "listName";
    static final String cardName = "Learning Postman";

    @BeforeAll
    static void setup() {
        var token = System.getenv("token");
        var key = System.getenv("authKey");

        reqSpec = new RequestSpecBuilder()
                .setBaseUri("https://api.trello.com")
                .addQueryParam("token", token)
                .addQueryParam("key", key)
                .setContentType(ContentType.JSON)
                .build();

        respSpec = new ResponseSpecBuilder()
                .expectStatusCode(200)
                .expectContentType(ContentType.JSON)
                .build();
    }

    @Test
    @Order(1)
    public void createBoard() {
        TrelloBoardEntity response = given()
                .spec(reqSpec)
                .basePath(urlForBoards)
                .queryParam("name", boardName)
                .when()
                .post()
                .then()
                .spec(respSpec)
                .body("name", startsWith(boardName))
                .log().all()
                .extract().body().as(TrelloBoardEntity.class);

        boardId = response.getId();

    }

    @Test
    @Order(2)
    public void getBoard() {
        given()
                .spec(reqSpec)
                .basePath(urlWithBoardId)
                .pathParam("id", boardId)
                .when()
                .get()
                .then()
                .spec(respSpec)
                .body("name", startsWith(boardName))
                .log().all();
    }

    @Test
    @Order(3)
    public void changeBoard() {
        given()
                .spec(reqSpec)
                .basePath(urlWithBoardId)
                .pathParam("id", boardId)
                .queryParam("name", newBoardName)
                .when()
                .put()
                .then()
                .spec(respSpec)
                .body("name", startsWith(newBoardName))
                .log().all();
    }

    @Test
    @Order(4)
    public void createList() {
        Response response = given()
                .spec(reqSpec)
                .basePath(urlForLists)
                .queryParam("idBoard", boardId)
                .queryParam("name", listName)
                .when()
                .post()
                .then()
                .spec(respSpec)
                .body("name", startsWith(listName))
                .log().all()
                .extract().response();

        JsonPath jsonPathEvaluator = response.jsonPath();
        listId = jsonPathEvaluator.get("id");
    }

    @Test
    @Order(5)
    public void createCard() {
        given()
                .spec(reqSpec)
                .basePath(urlForCards)
                .queryParam("idList", listId)
                .queryParam("closed", false)
                .queryParam("name", cardName)
                .when()
                .post()
                .then()
                .spec(respSpec)
                .body("name", startsWith(cardName))
                .log().all();
    }

    @AfterAll
    static void tearDown() {
        given()
                .spec(reqSpec)
                .basePath(urlWithBoardId)
                .pathParam("id", boardId)
                .when()
                .delete()
                .then()
                .spec(respSpec)
                .log().all();
    }
}
