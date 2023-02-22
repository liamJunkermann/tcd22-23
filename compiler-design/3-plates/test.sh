comp() {
    file=$1
    filename=${file%.*}
    extension=${file##*.}
    flex_output=$filename-lex.yy.c
    flex -o $flex_output $file
    gcc $flex_output -lfl -o $filename
    ./$filename
}
