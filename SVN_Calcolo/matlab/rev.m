% reverse modulo N

function x = rev(x)
x(2:length(x))=x(length(x):-1:2);