import QuestionAnswer.*
import androidx.compose.animation.core.*
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import data.ApiClient
import data.models.Lesson
import data.models.Quiz
import kotlinx.coroutines.launch

val apiClient = ApiClient()

sealed class Destination {
    class HomePage() : Destination() {
        companion object {
            var name: String = "Home"
        }
    }

    class Lessons() : Destination() {
        companion object {
            var name: String = "Lessons"
        }
    }
}

enum class QuestionAnswer{
    CORRECT,
    WRONG,
    NOT_ANSWER
}

val assetPath = "local_assets"
val rightAnswer = "Right Answer! You Won 5 points  \uD83E\uDD73"
val wrongAnswer = "Sorry wrong Answer \uD83D\uDE14, but you still go a point \uD83D\uDE0A"

@Composable
@Preview
fun App() {

    MaterialTheme {
        Image(
            painter = painterResource("home_bg.jpg"),
            "",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        HomePage()

    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomePage() {
    val purple = Color(0xFF4D149E)
    val colorSuccess = Color(0xff1cac4f)
    var totalTokens by remember { mutableStateOf(0) }

    val titleText = buildAnnotatedString {
        withStyle(style = SpanStyle(color = Color.White, fontWeight = FontWeight.ExtraLight)) {
            append("Win ")
        }
        withStyle(style = SpanStyle(color = colorSuccess, fontWeight = FontWeight.Bold)) {
            append("While ")
        }
        withStyle(style = SpanStyle(color = Color.White, fontWeight = FontWeight.ExtraBold)) {
            append("Learning ")
        }
        withStyle(style = SpanStyle(color = Color.White, fontWeight = FontWeight.ExtraLight)) {
            append("With \n")
        }
        withStyle(style = SpanStyle(color = Color.White, fontWeight = FontWeight.ExtraLight)) {
            append("Kiddie Block")
        }
    }

    val indexes = listOf(1, 2, 3, 4, 5, 6, 7, 8)
    var nav by remember { mutableStateOf(Destination.HomePage.name) }
    var lessons = remember { mutableStateListOf<Lesson>() }
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(false) }

    when (nav) {
        Destination.HomePage.name -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

                Column(
                    modifier = Modifier.fillMaxSize().padding(top = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val infiniteTransition = rememberInfiniteTransition()
                    val scale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.3f,
                        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse)
                    )
                    Text(titleText, modifier = Modifier.padding(30.dp), fontSize = 40.sp, textAlign = TextAlign.Center)
                    Image(
                        painter = painterResource("smiling_boy.png"),
                        "",
                        modifier = Modifier.size(300.dp).graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            transformOrigin = TransformOrigin.Center
                        })
                    Button(onClick = {
                        scope.launch {
                            loading = !loading
                            lessons.addAll(apiClient.getLessons().data)
                            nav = Destination.Lessons.name
                            loading = !loading
                        }
                    }, colors = ButtonDefaults.buttonColors(backgroundColor = Color.White, contentColor = purple)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            if (loading){
                                CircularProgressIndicator()
                            } else {
                                Text("Let's Go")
                                Icon(painter = painterResource("arrow_forward_.svg"), "", modifier = Modifier.size(15.dp))
                            }
                        }
                    }
                }

            }
        }
        else -> {
            var activeLessonIndex by remember { mutableStateOf(0) }

            var activeLessonImage by remember { mutableStateOf(-1) }
            val currentLesson by derivedStateOf {
                lessons[activeLessonIndex]
            }
            var currentQuiz = remember { Quiz(question = "",answers = emptyList(), correctAnswer = 0) }
            var showQuiz by remember { mutableStateOf(false) }
            var enableCurrentQuiz by remember { mutableStateOf(true) }
            var ended by remember { mutableStateOf(false) }
            Box(modifier = Modifier.fillMaxSize()) {

                Box(
                    modifier = Modifier.padding(start = 30.dp, end = 30.dp, top = 60.dp, bottom = 30.dp).fillMaxSize()
                        .background(Color.Transparent, shape = RoundedCornerShape(10.dp))
                ) {

                    Box(modifier = Modifier.fillMaxSize())
                    {

                        if (activeLessonImage == -1){
                            Image(painterResource("$assetPath/home.jpg"),"", modifier = Modifier.fillMaxSize().clip(
                                RoundedCornerShape(10.dp)
                            ), contentScale = ContentScale.FillBounds)
                        }
                        else if (showQuiz){
                            Column(verticalArrangement = Arrangement.spacedBy(40.dp), modifier = Modifier.align(Alignment.Center)) {
                                var answer = remember { mutableStateOf(NOT_ANSWER) }
                                var text by remember { mutableStateOf("Quiz Time \uD83D\uDE01") }

                                text = when(answer.value){
                                    CORRECT -> {
                                        rightAnswer
                                    }
                                    WRONG -> {
                                        wrongAnswer
                                    }
                                    NOT_ANSWER -> {
                                        "Quiz Time \uD83D\uDE01"
                                    }
                                }

                                Text(text, fontSize = 20.sp, fontWeight = FontWeight.Medium, color = Color.White)
                                Text(currentLesson.quize.question, fontSize = 40.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)

                                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                                    currentLesson.quize.answers.forEachIndexed { index, s ->
                                        var cardColor by remember { mutableStateOf(Color(0xFFFFFFFF)) }
                                        Card(modifier = Modifier.width(200.dp), backgroundColor = cardColor, onClick = {
                                            enableCurrentQuiz = false
                                            cardColor = if (index == currentLesson.quize.correctAnswer){
                                                answer.value = QuestionAnswer.CORRECT
                                                totalTokens += 5
                                                Color(0xFF45BA28)
                                            }else{
                                                answer.value = QuestionAnswer.WRONG
                                                totalTokens += 1
                                                Color(0xFFBA2828)
                                            }
                                        }, enabled = enableCurrentQuiz) {
                                            Text(s, modifier = Modifier.padding(15.dp))
                                        }
                                    }
                                }

                            }
                        }
                        else {
                            Image(painterResource("$assetPath/${currentLesson.images[activeLessonImage]}"),"", modifier = Modifier.fillMaxSize().clip(
                                RoundedCornerShape(10.dp)
                            ), contentScale = ContentScale.FillBounds)
                        }
                    }

                    Box (modifier = Modifier.align(Alignment.BottomCenter)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(80.dp), modifier = Modifier.padding(20.dp)) {
                            //Back
                            if (!showQuiz){
                                IconButton(onClick = {
                                    if (activeLessonIndex == 0){
                                        nav = Destination.HomePage.name
                                    }else{
                                        if (activeLessonImage != 0){
                                            activeLessonImage--
                                        }
                                    }
                                }){
                                    Image(painter = painterResource("local_assets/Btn-left.svg"),"")
                                }
                            }

                            val endText = buildAnnotatedString {
                                withStyle(style = SpanStyle(color = Color.Black, fontWeight = FontWeight.ExtraLight)) {
                                    append("That ")
                                }
                                withStyle(style = SpanStyle(color = colorSuccess, fontWeight = FontWeight.Bold)) {
                                    append("Was a ")
                                }
                                withStyle(style = SpanStyle(color = Color.Gray, fontWeight = FontWeight.ExtraBold)) {
                                    append("Great ")
                                }
                                withStyle(style = SpanStyle(color = Color.Black, fontWeight = FontWeight.ExtraLight)) {
                                    append("Tour \n")
                                }
                                withStyle(style = SpanStyle(color = Color.Black, fontWeight = FontWeight.ExtraLight)) {
                                    append("See You Again. Bye! \uD83D\uDC4B")
                                }
                            }
                            Dialog(onCloseRequest = {

                            }, visible = ended){
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(endText, textAlign = TextAlign.Center)
                                        IconButton(onClick = {
                                            nav = Destination.HomePage.name
                                            totalTokens = 0
                                            ended = false
                                        }){
                                            Image(painter = painterResource("local_assets/Btn-right.svg"),"")
                                        }
                                    }

                                }
                            }

                            //forward
                            IconButton(onClick = {
                                if (activeLessonImage == -1){
                                    activeLessonImage = 0
                                    return@IconButton
                                }
                                if (activeLessonImage < (currentLesson.images.size - 1)){
                                    //means there are still images
                                   activeLessonImage++
                                }else if(currentLesson.quize.answers.isNotEmpty() && !showQuiz) {
                                    currentQuiz = currentLesson.quize
                                    showQuiz = true
                                    enableCurrentQuiz = true
                                }else {
                                    showQuiz = false
                                    activeLessonImage = 0
                                    if (activeLessonIndex < (lessons.size - 1)){
                                        activeLessonIndex++
                                    }else{
                                        ended = true
                                    }
                                }
                                println("$activeLessonImage" + "Total Size : ${currentLesson.images.size}")
                            }){
                                Image(painter = painterResource("local_assets/Btn-right.svg"),"")
                            }
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 10.dp).align(
                        Alignment.BottomCenter)) {
                        Image(painter = painterResource("gold_coin.svg"),"", modifier = Modifier.size(24.dp))
                        Text("$totalTokens Tokens Won", modifier = Modifier.padding(8.dp), fontWeight = FontWeight.ExtraBold, color = Color.White)
                    }
                }



            }

        }
    }
}

fun main() = application {
    val windowState = rememberWindowState(height = 850.dp)
    Window(onCloseRequest = ::exitApplication, title = "Kiddie Block", state = windowState) {
        App()
    }
}
