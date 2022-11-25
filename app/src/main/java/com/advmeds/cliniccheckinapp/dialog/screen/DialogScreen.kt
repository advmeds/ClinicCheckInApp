package com.advmeds.cliniccheckinapp.dialog.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.SnackbarDefaults.backgroundColor
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.advmeds.cliniccheckinapp.R

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CheckingDialogFragmentScreen() {
    val openDialog = remember { mutableStateOf(true) }

    if (openDialog.value) {

        Dialog(onDismissRequest = {
            openDialog.value = false
        },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = false,
            ),
            content = {
                Card(
                    modifier = Modifier.padding(start = 146.dp, end = 146.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(20.dp), verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_baseline_change_circle),
                            contentDescription = "change_circle"
                        )
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = stringResource(id = R.string.checking),
                            color = colorResource(id = R.color.dark_gray),
                            fontSize = 72.sp,
                        )
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ErrorDialogFragmentScreen(message: String, closeDialog: () -> Unit) {

    val openDialog = remember { mutableStateOf(true) }

    if (openDialog.value) {

        Dialog(onDismissRequest = {
            openDialog.value = false
            closeDialog()
        },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = false,
            ),
            content = {
                Card(
                    modifier = Modifier
                        .padding(start = 146.dp, end = 146.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                {
                    Row(
                        modifier = Modifier
                            .padding(20.dp), verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_baseline_error),
                            contentDescription = "error"
                        )
                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = stringResource(id = R.string.fail_to_check),
                                color = colorResource(id = R.color.error),
                                fontSize = 48.sp
                            )

                            Text(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                text = if (message == null || message == "") "尚未到 下午 報到時間，請於 13:00~17:00 前往報到 或洽服務人員" else message,
                                color = Color.Black,
                                fontSize = 36.sp
                            )
                        }
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SuccessDialogFragmentScreen(closeDialog: () -> Unit) {

    val openDialog = remember { mutableStateOf(true) }

    if (openDialog.value) {

        Dialog(onDismissRequest = {
            openDialog.value = false
            closeDialog()
        },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = false,
            ),
            content =
            {
                Card(
                    modifier = Modifier
                        .padding(start = 146.dp, end = 146.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(20.dp), verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_baseline_check_circle),
                            contentDescription = "check_circle"
                        )
                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = stringResource(id = R.string.success_to_check),
                                color = colorResource(id = R.color.colorPrimary),
                                fontSize = 48.sp
                            )

                            Text(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                text = stringResource(id = R.string.success_to_check_message),
                                color = Color.Black,
                                fontSize = 36.sp
                            )
                        }
                    }
                }
            }
        )
    }
}