function xr=deasysolve(f,p,l,i,s,c)

xl=1;
xr=2*xl;
dfxr=diff(f(p,[xr,xr+1],l,i,s,c));
while(dfxr>=0)
    xl=xr;
    xr=2*xl;
    dfxr=diff(f(p,[xr,xr+1],l,i,s,c));
end
%xl
%xr
%pause(5)
while(xr-xl>1)
    xm=(xl+xr)/2;
    %pause(1)
    dfxm=diff(f(p,[xm,xm+1],l,i,s,c));
    if (dfxm>0)
        xl=xm;
    elseif (dfxm<0)
        xr=xm;
    else
        break;
    end
    
end
%xr