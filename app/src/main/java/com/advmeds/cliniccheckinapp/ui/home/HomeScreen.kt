package com.advmeds.cliniccheckinapp.ui.home

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.advmeds.cliniccheckinapp.R
import com.airbnb.lottie.compose.*
import kotlin.text.substring


@Composable
fun HomeScreen(
    onNextClick: () -> Unit,
    onMadeRequest: (String) -> Unit,
    input_title: AnnotatedString,
    input_hint: String,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopCard(onNextClick)
        MiddleCard(input_title, input_hint, onMadeRequest)
    }
}

@Composable
private fun TopCard(onNextClick: () -> Unit) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp, 20.dp, 20.dp, 0.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { onNextClick() },
                )
            },
        shape = RoundedCornerShape(10.dp),
        elevation = 8.dp,
    )
    {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "logo",
                modifier = Modifier.padding(0.dp, 20.dp)
            )
            Text(
                text = stringResource(id = R.string.app_name),
                modifier = Modifier.padding(40.dp, 10.dp, 0.dp, 10.dp),
                maxLines = 1,
                color = colorResource(id = R.color.colorPrimary),
                fontSize = 60.sp,
                fontWeight = FontWeight.Bold
            )
        }

    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun MiddleCard(
    input_title: AnnotatedString,
    input_hint: String,
    onMadeRequest: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        shape = RoundedCornerShape(10.dp),
        elevation = 8.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            var inputText by remember { mutableStateOf("") }

            Row(
                modifier = Modifier.padding(0.dp, 20.dp, 0.dp, 0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.present_card_remind),
                    maxLines = 1,
                    color = colorResource(id = R.color.colorPrimary),
                    fontSize = 48.sp
                )
                Box(modifier = Modifier.padding(40.dp, 0.dp, 0.dp, 0.dp)) {
                    GetCardPresentLottieAnimation()
                }
            }

            Text(
                text = input_title,
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                textAlign = TextAlign.Center,
                fontSize = 36.sp
            )

            val keyboardController = LocalSoftwareKeyboardController.current
            val focusManager = LocalFocusManager.current

            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = colorResource(id = R.color.input_border_color)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp, 10.dp, 20.dp, 16.dp)

                    // Hide keyboard and clear focus in text field

                    .onFocusEvent {
                        keyboardController?.hide()
                    }
                    .onFocusChanged { focusState ->
                        when {
                            focusState.isFocused -> {
                                keyboardController?.hide()
                                focusManager.clearFocus(true)
                            }
                            focusState.hasFocus -> {
                                keyboardController?.hide()
                                focusManager.clearFocus(true)
                            }
                            focusState.isCaptured -> {
                                keyboardController?.hide()
                                focusManager.clearFocus(true)
                            }
                        }
                    },
                placeholder = {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        text = input_hint,
                        fontSize = 36.sp
                    )
                },
                textStyle = TextStyle(fontSize = 36.sp, textAlign = TextAlign.Center),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                )
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = colorResource(id = R.color.keyboard_background))
            ) {
                // English KeyBoard
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(7f)
                        .padding(0.dp, 16.dp)
                ) {

                    val englishSymbolList: MutableList<String> = getStringAlphabetList()

                    englishSymbolList.add("-")
                    englishSymbolList.add("empty")

                    for (i in 0 until englishSymbolList.size step 7) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(20.dp, 0.dp),
                            horizontalArrangement = Arrangement.Start
                        ) {

                            englishSymbolList.filterIndexed({ index, _ -> index >= i && index < (i + 7) })
                                .forEach {
                                    if (it.equals("empty")) {
                                        EmptyKey(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .weight(1f)
                                        )
                                        return@forEach
                                    }

                                    Key(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .weight(1f),
                                        label = it,
                                        onClick = { inputText = "${inputText}${it}" })
                                }
                        }
                    }
                }

                // Num pad
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(3f)
                        .padding(0.dp, 16.dp)
                ) {
                    val numberList: MutableList<String> = getNumberList()
                    numberList.add("empty")
                    numberList.add("0")
                    numberList.add("empty")

                    for (i in 0 until numberList.size step 3) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(20.dp, 0.dp),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            numberList.filterIndexed({ index, _ -> index >= i && index < (i + 3) })
                                .forEach {
                                    if (it == "empty") {
                                        EmptyKey(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .weight(1f)
                                        )
                                        return@forEach
                                    }

                                    Key(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .weight(1f),
                                        label = it,
                                        onClick = { inputText = "${inputText}${it}" })
                                }
                        }
                    }
                }

                // Buttons pad
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .padding(vertical = 16.dp, horizontal = 20.dp),
                ) {
                    val shape = RoundedCornerShape(4.dp)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(bottom = 6.dp)
                            .align(Alignment.CenterHorizontally)
                    ) {
                        Column(
                            modifier = Modifier
                                .clickable {
                                    inputText = inputText.dropLast(1)
                                }
                                .fillMaxSize()
                                .clip(shape)
                                .background(White)
                                .padding(bottom = 6.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                modifier = Modifier.fillMaxWidth(),
                                painter = painterResource(id = R.drawable.ic_baseline_backspace),
                                contentDescription = "ic_baseline_backspace"
                            )

                            Text(
                                text = stringResource(id = R.string.backspace),
                                color = colorResource(id = R.color.keyboard_text_color),
                                fontSize = 28.sp
                            )
                        }
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable(onClick = {
                                    onMadeRequest(
                                        inputText
                                            .trim()
                                    )
                                    inputText = ""
                                })
                                .clip(shape)
                                .background(White),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                modifier = Modifier.fillMaxWidth(),
                                painter = painterResource(id = R.drawable.ic_baseline_arrow_forward),
                                contentDescription = "ic_baseline_arrow_forward"
                            )

                            Text(
                                text = stringResource(id = R.string.enter),
                                color = colorResource(id = R.color.keyboard_text_color),
                                fontSize = 28.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GetCardPresentLottieAnimation() {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.card_present))
    LottieAnimation(
        composition = composition,
        iterations = Int.MAX_VALUE
    )
}

// generate list of numbers
private fun getNumberList(): MutableList<String> {
    val charNumberList = ('1'..'9').toMutableList()
    val stringList = mutableListOf<String>()

    for (i in charNumberList) {
        stringList.add(i.toString())
    }

    return stringList
}

// generate list of chapters
private fun getStringAlphabetList(): MutableList<String> {
    val charEnglishList = ('A'..'Z').toMutableList()
    val stringList = mutableListOf<String>()

    for (i in charEnglishList) {
        stringList.add(i.toString())
    }

    return stringList
}

// chapter or number button
@Composable
private fun Key(modifier: Modifier = Modifier, label: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(4.dp)

    Box(
        modifier = modifier
            .padding(2.dp)
            .clip(shape)
            .clickable(onClick = onClick)
            .background(White)
            .padding(vertical = 12.dp, horizontal = 4.dp), contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 36.sp,
            color = colorResource(id = R.color.keyboard_text_color)
        )
    }
}

// view of empty button
@Composable
private fun EmptyKey(modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(4.dp)

    Box(
        modifier = modifier
            .padding(2.dp)
            .clip(shape)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "", fontSize = 36.sp)
    }
}
