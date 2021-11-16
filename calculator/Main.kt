package calculator

import java.math.BigInteger

class Calculator {
    /** Map used to store variables**/
    private val variables = mutableMapOf<String, BigInteger>()
    /** Checks for commands **/
    private fun checkCommand(s: String): Boolean{
        if (s[0] == '/'){
            return when {
                "exit" in s || "help" in s -> false
                else -> {println("Unknown command"); true
                }
            }
        }
        return false
    }

    /** takes input string and stores, checks for validation, then stores variable **/
    private fun storeVariables(s: String): Boolean{

        if (invalid(s)) return true
        else {
            if (!invalid(s)) {
                val b = s.substringBefore('=').filter { it != ' ' }
                val after = s.substringAfter('=').filter { it != ' ' }

                if (after in variables.keys){variables[b] = variables[after]!!; return false}
                try {
                    variables[b] = after.toBigInteger()
                } catch (e: Exception) {
                    println("Unknown variable")
                    return true
                }
            }
        }
        return false
    }

    /** Validates string for saving variables **/
    private fun invalid (s: String): Boolean {

        val temp: List<String> = s.split("")
        val first: Int = temp.indexOf("=")
        val last = temp.lastIndexOf("=")
        if (first != last){
            println("Invalid assignment")
            return true
        }
        val b = s.substringBefore('=').filter {it != ' '}
        val after = s.substringAfter('=').filter {it != ' '}
        val regNums = "[0-9]".toRegex()
        val regLet = "[a-zA-Z]".toRegex()
        when {
            !b.contains("[a-zA-Z0-9]".toRegex()) -> {println("Invalid Identifier"); return true}
            b.contains(regNums) -> {println("Invalid identifier"); return true}
            !after.contains("[a-zA-Z0-9]".toRegex()) -> {println("Invalid assignment"); return true}
            after.contains(regLet) && after.contains(regNums) -> {println("Invalid assignment"); return true}

        }
        return false
    }

    /** When accessing variables, it will return its value if it is stored in the variable map **/
    private fun returnValue(list: List<String>): Boolean{
        val temp: List<String> = list.filter { it != " " }
        if (temp[0] in variables.keys) {println(variables[temp[0]]!!); return false }

        println("Unknown variable")
        return true
    }

    /** creates a postfix list to implement into a stack for computation **/
    private fun infixToPostfix(s: String): String{
        var r = 0 //instantiate r, this will be the index of the next value.
        var temp = ""
        var numOfOpenParens = 0
        val ops = mutableListOf<String>()
        val postfix = mutableListOf<String>()
        val regLetNum = "[a-zA-Z0-9]".toRegex()


        for (i in s.indices){
            if (s[i].toString() == " ") {r = i + 1; continue} // if empty then r is set to the next index after i and continue loop
            if (temp.isEmpty()) temp += s[i] // if temp is blank, add l as first char

            if (s[i].toString().contains(regLetNum)){ // if s[i] is a letter or number
                // use this loop to grab numbers larger than 9 or variables with more than 1 letter
                if (i != s.length - 1 && s[i + 1].toString().contains(regLetNum)){ // check if its next character is also a letter or num
                    r++ // increment r
                    temp += s[r] // add that char to temp
                }
                else { // if the next char is not a let or num then add it to postfix, reset temp and increment r
                    postfix += temp
                    temp = ""
                    r = i + 1
                }
            } else if (temp[0] == '+'){ // check for multiple + signs and only use 1 or them
                if(s[i + 1] != '+'){
                    if (ops.isEmpty()){
                        ops += temp
                        temp = ""
                        r = i + 1
                    }else {

                        while ( ops.isNotEmpty() ) {
                            if (ops.last() != "(") postfix += ops.removeLast()
                            else break
                        }
                        ops += temp
                        temp = ""
                        r = i + 1
                    }
                }
            } else if (temp[0] == '-'){ // check for multiple minus signs and add + or - based on odd or even
                if (s[i + 1] == '-'){
                    r++
                    temp += s[r]
                } else
                    when {
                        temp.length % 3 == 0 || temp.length == 1 -> {
                            if (ops.isEmpty()){
                                ops += "-"
                                temp = ""
                                r = i + 1
                            }else
                            {
                                while (ops.isNotEmpty()) {
                                    if (ops.last() != "(") postfix += ops.removeLast()
                                    else break
                            }
                                ops += "-"
                                temp = ""
                                r = i + 1
                            }

                    }
                        temp.length % 2 == 0 -> {
                            if (ops.isEmpty()){
                                ops += "+"
                                temp = ""
                                r = i + 1
                            }else
                            {
                                while (ops.isNotEmpty()) {
                                    if (ops.last() != "(") postfix += ops.removeLast()
                                    else break
                                }
                                ops += "+"
                                temp = ""
                                r = i + 1
                            }
                        }
                }
            }
            else if (temp[0] == '*') { // if more than one * then return false, otherwise if there's a - or + then add it stack so it's ahead of them
                if (s[i + 1] == '*') return "false"
                else if (ops.isEmpty()) {
                    ops += "*"
                    temp = ""
                    r = i + 1
                } else if (!ops.last().contains("[+\\-(]")) {
                    ops += "*"
                    temp = ""
                    r = i + 1
                } else {
                    while (ops.isNotEmpty() || ops.last() != "(") { // check for parens to so you know scope
                        postfix += ops.removeLast()
                    }
                    ops += "*"
                    temp = ""
                    r = i + 1
                }
            }
            else if (temp[0] == '/') { // same as multiply
                if (s[i + 1] == '/') return "false"
                else if (ops.isEmpty()) {
                    ops += "/"
                    temp = ""
                    r = i + 1
                } else if (!ops.last().contains("[+\\-(]")) {
                    ops += "/"
                    temp = ""
                    r = i + 1
                } else {
                    while (ops.isNotEmpty() || ops.last() != "(") {
                        postfix += ops.removeLast()
                    }
                    ops += "/"
                    temp = ""
                    r = i + 1
                }
            }
            else if (temp[0] == '(') { // add it to the stack to start a new scope and add 1 to the open parens quantity
                numOfOpenParens++
                ops += "("
                temp = ""
                r = i + 1
            }
            else if (temp[0] == ')') { // pop everything from the stack until it hits an open parens. decrement open parens
                if (ops.isEmpty()) return "false"
                while (ops.last() != "("){
                    postfix += ops.removeLast()
                    if (ops.isEmpty()) return "false"
                }
                numOfOpenParens--
                if (numOfOpenParens < 0 ) return "false"
                ops.removeLast()
                temp = ""
                r = i + 1
            }
        }
        while (ops.isNotEmpty()) postfix += ops.removeLast() // after we've gone through the whole string, pop everything left off the stack and onto postfix
        if (numOfOpenParens > 0) return "false" // if there are still open parens than it is invalid
        return postfix.joinToString(" ") // return the postfix notation
    }

    private fun calculate (s: String): String {
        var res = BigInteger.valueOf(0) // instantiate a result

        if (infixToPostfix(s) == "false") return "Invalid expression"
        val post = infixToPostfix(s).split(" ")
        val stack = mutableListOf<BigInteger>() // stack for processing the postfix

        for (i in post){
            when {
                i.contains("[0-9]".toRegex()) -> stack += i.toBigInteger() // if its a num add it to stack
                i.contains("[a-zA-Z]".toRegex()) -> stack += variables[i]!! // if its a variable, add its value to the stack
                else -> {
                    val second = stack.removeLast()
                    val first = stack.removeLast()
                    var new: BigInteger
                    when (i){
                        "+" -> {new = first + second; stack += new} // calculate then add new total to stack
                        "-" -> {new = first - second; stack += new} // calculate then add new total to stack
                        "*" -> {new = first * second; stack += new} // calculate then add new total to stack
                        "/" -> {new = first / second; stack += new} // calculate then add new total to stack
                    }
                }
            }
        }
        res += stack.first() // res is the final calculated amount

        return res.toString() // return its value as a string
    }

    fun run () {
        while(true){
            val input = readLine()!! // get user input
            val nums = input.split(" ").filter {it.contains("[a-zA-Z0-9]".toRegex())}

            when {

                input == "" -> continue // if nothing then continue loop
                input == "/exit" -> {println("Bye!"); break} // exit program
                input == "/help" -> println("The program calculates stuff") // what it does
                (checkCommand(input)) -> continue // check if its an invalid command
                nums.size == 1 && '=' !in input  -> { // check for a single value
                    if (returnValue(nums)) continue // if it returns true from the function then its invalid, otherwise print the value
                }
                '=' in input -> { // if it has an equals sign than its for setting vars
                    if (storeVariables(input)) continue // try to store the variable. If it returns true then just continue
                }
                else -> {if (calculate(input) == "false") continue; else println(calculate(input))} // if calculate returns the false string then just continue, else calculate the input
            }
        }
    }
}




fun main() {
    val calc = Calculator()
    calc.run()
}
