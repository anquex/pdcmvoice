function s=formatme(n)

%{
% find min 10 power greater than n
i=1;
k=1;
while (n>k)
    i=i+1;
    k=k*10;
end
%}
if n<=1e-03
    s='1.e';
else
    s='.3f';
end