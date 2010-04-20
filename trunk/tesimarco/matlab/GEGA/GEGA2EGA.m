function MLE=GEGA2EGA(GMLE,allowed)

% verificato, algoritmo corretto
%
%
%
%

[m,n,slots]=size(GMLE);
if (m~=n)
    error('tabella non valida')
else
    T=m-1;
end
clear m;
clear n;

p=1./(2.^[1:slots]);
MLE=zeros(T,T,slots);
% giusto per scrupolo
allowed=unique(allowed);


for l=1:slots
    for c=0:T
        for s=0:T-c
            i=T-s-c;
            ipos=bisection_search(allowed,GMLE(c+1,s+1,l));
            n1=0;
            if(ipos~=0)
                n1=allowed(ipos);
            end
            
            if(ipos~=length(allowed))
                n2=allowed(ipos+1);
            else
                n2=0;
            end
            if (n1==0 || n2==0)
                n=max(n1,n2);
            else
                y1=fitness(p,n1,l,c,s,i);
                y2=fitness(p,n2,l,c,s,i);
                if (y1>y2)
                    n=n1;
                else
                    n=n2;
                end
                MLE(c+1,s+1,l)=n;
            end
        end
    end
end
