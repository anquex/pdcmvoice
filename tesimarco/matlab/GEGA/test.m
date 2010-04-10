clc
b=2;
l=10;
p=1./(b.^(1:60));

sfG0=sfG(l);

sficsT0='((1-p(l))^n)^i * (n*p(l)*(1-p(l))^(n-1))^s * (1-(1-p(l))^n-n*p(l)*(1-p(l))^(n-1))^c';
sfG1=diff(sfG0,'n');
sficsT1=diff(sficsT0,'n');

sfG1=char(sfG1);
sficsT1=char(sficsT1);

sfT0=[sficsT0 ' * '  sfG0];
%sfT1=[sficsT0 ' * ' sfG1 ' + ' sficsT1 ' * ' sfG0];
sfT1=diff(sfT0,'n');

% (1-p(l))^n
% n*p(l)*(1-p(l))^(n-1)
% (1-(1-p(l))^n-n*p(l)*(1-p(l))^(n-1))
% ((1-p(l))^n)^i*(n*p(l)*(1-p(l))^(n-1))^s*((1-(1-p(l))^n-n*p(l)*(1-p(l))^(n-1)))^c


sfT1=char(sfT1);
sfT0=char(sfT0);

sfT1=strrep(sfT1,'*','.*');
sfT1=strrep(sfT1,'^','.^');
sfT1=strrep(sfT1,'/','./');
sfT0=strrep(sfT0,'*','.*');
sfT0=strrep(sfT0,'^','.^');
sfT0=strrep(sfT0,'/','./');


%fT1=inline(sfT1,)
fT0=inline(sfT0,'p','n','l','i','s','c');
%fT0(p,1024,10,8,8,8)
fT1=inline(sfT1,'p','n','l','i','s','c');

fT0(p,1024,l,8,8,5)
%fT1(p,1024,l,8,8,5)
tic
[xmax, fxmax]=deasysolve(fT0,p,l,8,4,7)
toc
%fzero(@(n)fT1(p,n,10,8,8,0),1000,{'TolX',10^-55})
