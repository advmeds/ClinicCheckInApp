package com.advmeds.cliniccheckinapp.dialog.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.advmeds.cliniccheckinapp.R

@Composable
fun CheckingDialogFragmentScreen() {
    Box(modifier = Modifier.padding(start = 146.dp, end = 146.dp),contentAlignment = Alignment.Center ) {
        Card(
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
}