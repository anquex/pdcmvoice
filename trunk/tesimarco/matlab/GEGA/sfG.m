
% function that generate greenberg stop probability in slot l
% it is the deployment of a string

function s=sfG(l)

% (1-p(i))^n
% n*p(i)*(1-p(i))^(n-1)
%

s='1';
for i=1:l-1
    s=[s '*' '(1-(1-p(' int2str(i) '))^n-n*p(' int2str(i) ')*(1-p(' int2str(i) '))^(n-1))'];
end

s=[s '* ((1-p(' int2str(l) '))^n+n*p(' int2str(l) ')*(1-p(' int2str(l) '))^(n-1))' ]; 