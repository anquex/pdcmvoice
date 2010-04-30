function xr=deasysolve2(f,p,l,i,s,c,offset)

xl=offset;
if (diff(f(p,[offset,offset+1],l,i,s,c))<0)
    xr=xl;
    return;
end
len=1;
xr=offset+len;
dfxr=diff(f(p,[xr,xr+1],l,i,s,c));
while(dfxr>0)
    xl=offset+len;
    len=bitshift(len,1);
    xr=offset+len;
    dfxr=diff(f(p,[xr,xr+1],l,i,s,c));
end
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