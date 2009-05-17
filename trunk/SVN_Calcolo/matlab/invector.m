function v = rev(x)

l=length(x);
v=x(1:l/2);
v = [];
for j=1:length(x)
    v(j) = x(length(x)-j+1);
end