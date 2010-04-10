function [xmax, fxmax]=easysolve(f,p,l,i,s,c)


x=1;
x2=2*x;

fx=f(p,x,l,i,s,c);
fx2=f(p,x2,l,i,s,c);
while(fx<fx2)
    x=x2;
    x2=2*x;
    fx=f(p,x,l,i,s,c);
    fx2=f(p,x2,l,i,s,c);
end

xmax=max(1,x/2);
fxmax=f(p,xmax,l,i,s,c);
x=xmax+1;
fx=f(p,x,l,i,s,c);

while (fxmax<fx)
    xmax=x;
    fxmax=fx;
    x=xmax+1;
    fx=f(p,x,l,i,s,c);
end
