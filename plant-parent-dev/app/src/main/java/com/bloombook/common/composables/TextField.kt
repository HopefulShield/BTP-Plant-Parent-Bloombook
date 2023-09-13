package com.bloombook.common.composables

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import com.bloombook.ui.theme.RobotoFamily


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomTextField(
    placeholder: String,
    value: String,
    onNewValue: (String) -> Unit,
    modifier: Modifier,
)
{
    TextField(
        value = value,
        onValueChange = { onNewValue(it)},

        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
        },

        // smooth the edges
        shape = RoundedCornerShape(24),

        // add padding, increase width, and add shadow
        modifier = modifier,

        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor = Color.White,
            cursorColor = Color.Gray,
            focusedTextColor = Color.Gray,
            unfocusedTextColor = Color.Gray,
            selectionColors = TextSelectionColors(handleColor = Color.Gray, backgroundColor = Color.Transparent),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        textStyle = MaterialTheme.typography.bodyLarge,
        maxLines = 1,
    )
}





@Composable
fun NameField(placeholder:String, value:String, onNewValue: (String) -> Unit, modifier:Modifier) {
    CustomTextField(placeholder, value, onNewValue, modifier)
}

@Composable
fun EmailField(placeholder:String, value:String, onNewValue: (String) -> Unit, modifier:Modifier) {
    CustomTextField(placeholder, value, onNewValue, modifier)
}

//@Composable
//fun PasswordField(placeholder:String, value:String, onNewValue: (String) -> Unit, modifier:Modifier) {
//    CustomTextField(placeholder, value, onNewValue, modifier)
//}

@Composable
fun ConfirmPasswordField(placeholder:String, value:String, onNewValue: (String) -> Unit, modifier:Modifier) {
    CustomTextField(placeholder, value, onNewValue, modifier)
}

@Composable
fun ObservationField(placeholder: String, value: String, onNewValue: (String) -> Unit, modifier: Modifier) {
    CustomTextField(placeholder, value, onNewValue, modifier)
}

@Composable
fun ReminderField(placeholder:String, value:String, onNewValue: (String) -> Unit, modifier:Modifier) {
    CustomTextField(placeholder, value, onNewValue, modifier)
}

@Composable
fun BigTextField(placeholder:String, value:String, onNewValue: (String) -> Unit, modifier:Modifier) {
    CustomTextField(placeholder, value, onNewValue, modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomTextField2(
    placeholder: String,
    value: String,
    onNewValue: (String) -> Unit,
    modifier: Modifier,
    keyboardType: KeyboardType,
    maxLines: Int,
    shape: Shape
)
{
    TextField(
        value = value,
        onValueChange = { onNewValue(it)},

        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyLarge,
                /*
                fontFamily = RobotoFamily,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.W300,

                 */
                color = Color.Gray
            )
        },

        // smooth the edges
        shape = shape,

        // add padding, increase width, and add shadow
        modifier = modifier,

        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor = Color.White,
            cursorColor = Color.Gray,
            focusedTextColor = Color.Gray,
            unfocusedTextColor = Color.Gray,
            selectionColors = TextSelectionColors(handleColor = Color.Gray, backgroundColor = Color.Transparent),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        textStyle = MaterialTheme.typography.bodyLarge,
        //TextStyle(textDecoration = TextDecoration.None , fontSize = 16.sp, fontWeight = FontWeight.W300),
        maxLines = maxLines,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.None,
            autoCorrect = true,
            keyboardType = keyboardType,
        ),
    )
}

@Composable
fun CustomField2(
    placeholder:String,
    value:String,
    onNewValue: (String) -> Unit,
    modifier:Modifier,
    keyboardType: KeyboardType,
    maxLines: Int,
    shape: Shape
) {
    CustomTextField2(placeholder, value, onNewValue, modifier, keyboardType, maxLines, shape)
}

@Composable
fun PasswordField(
    placeholder: String,
    value: String,
    onNewValue: (String) -> Unit,
    modifier: Modifier,
)
{
    var passwordVisible by remember { mutableStateOf(false) }

    TextField(
        value = value,
        onValueChange = { onNewValue(it)},

        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyLarge,
                /*
                fontFamily = RobotoFamily,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.W300,

                 */
                color = Color.Gray
            )
        },

        // smooth the edges
        shape = RoundedCornerShape(24),

        // add padding, increase width, and add shadow
        modifier = modifier,

        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor = Color.White,
            cursorColor = Color.Gray,
            focusedTextColor = Color.Gray,
            unfocusedTextColor = Color.Gray,
            selectionColors = TextSelectionColors(handleColor = Color.Gray, backgroundColor = Color.Transparent),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        textStyle = MaterialTheme.typography.bodyLarge,
        //TextStyle(textDecoration = TextDecoration.None , fontSize = 16.sp, fontWeight = FontWeight.W300),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.None,
            autoCorrect = true,
            keyboardType = KeyboardType.Password,
        ),
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            val image = if (passwordVisible)
                Icons.Filled.Visibility
            else Icons.Filled.VisibilityOff

            // Please provide localized description for accessibility services
            val description = if (passwordVisible) "Hide password" else "Show password"

            IconButton(onClick = {passwordVisible = !passwordVisible}){
                Icon(imageVector  = image, description)
            }
        }

        )
}
