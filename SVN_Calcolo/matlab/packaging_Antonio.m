
x=[1:10:100];
n=2;
ax = [n];
for i=2:6
    n = n*2;
    ax = [ax,n];
end
ax

f1=inline('x.*log(x)');
f2=inline('x+x/2.*log(x/2)');
plot(x,f1(x),'r',x,f2(x),'b');
set(gca,'XTick',ax)
%axis([0  pi/2  0  5])
%set(gca,'XTick',2:2:100)
%set(gca,'XTickLabel',{'2','4','16','32','64'})